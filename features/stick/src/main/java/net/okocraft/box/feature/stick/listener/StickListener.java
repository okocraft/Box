package net.okocraft.box.feature.stick.listener;

import com.github.siroshun09.configapi.api.value.ConfigValue;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.stick.integration.LWCIntegration;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Container;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StickListener implements Listener {

    private static final ConfigValue<String> MENU_COMMAND_SETTING =
            config -> config.getString("stick.menu-command", "box gui");

    private final BoxStickItem boxStickItem;

    public StickListener(@NotNull BoxStickItem boxStickItem) {
        this.boxStickItem = boxStickItem;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (event.getAction() == Action.PHYSICAL || event.getAction().isLeftClick() ||
                BoxProvider.get().isDisabledWorld(player) || !player.hasPermission("box.stick.menu")) {
            return;
        }

        var mainHand = player.getInventory().getItemInMainHand();
        var offHand = player.getInventory().getItemInOffHand();

        if ((event.getHand() == EquipmentSlot.HAND && boxStickItem.check(mainHand)) ||
                (event.getHand() == EquipmentSlot.OFF_HAND && mainHand.getType().isAir() && boxStickItem.check(offHand))) {
            var command = BoxProvider.get().getConfiguration().get(MENU_COMMAND_SETTING);

            if (!command.isEmpty()) {
                Bukkit.dispatchCommand(player, command);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractBlock(@NotNull PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (event.getAction() == Action.PHYSICAL || !event.getAction().isLeftClick() ||
                BoxProvider.get().isDisabledWorld(player) || !player.hasPermission("box.stick.container")) {
            return;
        }

        var mainHand = player.getInventory().getItemInMainHand();
        var offHand = player.getInventory().getItemInOffHand();

        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY ||
                !event.getPlayer().isSneaking() || event.getHand() != EquipmentSlot.HAND || !boxStickItem.check(offHand)) {
            return;
        }

        var block = event.getClickedBlock();

        // current containers: Barrel, BlastFurnace, BrewingStand, Chest, Dispenser, Dropper, Furnace, Hopper, ShulkerBox, and Smoker
        if (block == null || !(block.getState() instanceof Container container) || container.isLocked()) {
            return;
        }

        var view = new DummyInventoryView(player, container.getInventory());

        // for WorldGuard (flag: chest-access)
        if (!checkChestAccess(view)) {
            return;
        }

        var deposit = mainHand.getType().isAir();

        if (!LWCIntegration.canModifyInventory(player, container, deposit)) {
            return;
        }

        var playerMap = BoxProvider.get().getBoxPlayerMap();

        if (!playerMap.isLoaded(player)) {
            return;
        }

        var boxPlayer = playerMap.get(player);

        if (player.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true); // This prevents the instant breaking of blocks in creative mode.
        }

        boolean modified = false;

        if (view.getTopInventory() instanceof FurnaceInventory furnaceInventory) { // BlastFurnace, Furnace, and Smoker
            if (deposit) {
                modified = takeResultItem(boxPlayer, furnaceInventory);
            } else {
                modified = putFuel(boxPlayer, furnaceInventory, mainHand);
            }
        } else if (view.getTopInventory() instanceof BrewerInventory brewerInventory) { // BrewingStand
            if (deposit) {
                modified = takeResultPotions(boxPlayer, brewerInventory);
            } else if (isPotion(mainHand.getType())) {
                modified = putPotions(boxPlayer, brewerInventory, mainHand);
            } else if (mainHand.getType() == Material.BLAZE_POWDER) {
                modified = putBlazePowder(boxPlayer, brewerInventory);
            }
        } else { // other containers (Barrel, Chest, Dispenser, Dropper, Hopper, and ShulkerBox
            if (deposit) {
                modified = depositItemsInInventory(boxPlayer, view);
            } else {
                var boxItem = BoxProvider.get().getItemManager().getBoxItem(mainHand);

                if (boxItem.isPresent()) {
                    modified = withdrawToInventory(boxPlayer, view, boxItem.get());
                }
            }
        }

        if (modified) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // ignore POWDER_SNOW because it cannot be replenished
        if (event.getBlockPlaced().getType() == Material.POWDER_SNOW) {
            return;
        }

        var player = event.getPlayer();
        var mainHandItem = player.getInventory().getItemInMainHand();

        // ignore tools (flint and steel, axes, hoes, and shovels)
        if (0 < mainHandItem.getType().getMaxDurability()) {
            return;
        }

        if (!checkPlayerCondition(player, "box.stick.block")) {
            return;
        }

        if (event.getItemInHand().equals(mainHandItem) &&
                tryConsumingStock(player, mainHandItem)) {
            player.getInventory().setItemInMainHand(mainHandItem.clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemConsume(@NotNull PlayerItemConsumeEvent event) {
        var player = event.getPlayer();

        if (!checkPlayerCondition(player, "box.stick.food")) {
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();

        if (event.getItem().equals(mainHandItem) && tryConsumingStock(player, mainHandItem)) {
            event.setReplacement(mainHandItem.clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemBreak(@NotNull PlayerItemBreakEvent event) {
        var player = event.getPlayer();

        if (!checkPlayerCondition(player, "box.stick.tool")) {
            return;
        }

        var original = event.getBrokenItem();
        var copied = original.clone();

        copied.editMeta(Damageable.class, meta -> meta.setDamage(0));

        if (tryConsumingStock(player, copied)) {
            original.setAmount(2);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileLaunch(@NotNull ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        var permissionNodeSuffix =
                switch (event.getEntity().getType()) {
                    case EGG -> "egg";
                    case ENDER_PEARL -> "enderpearl";
                    case FIREWORK -> "firework";
                    case SNOWBALL -> "snowball";
                    case SPLASH_POTION -> "potion";
                    case THROWN_EXP_BOTTLE -> "expbottle";
                    default -> null;
                };

        if (permissionNodeSuffix == null || !checkPlayerCondition(player, "box.stick." + permissionNodeSuffix)) {
            return;
        }

        var mainHand = player.getInventory().getItemInMainHand();

        if (tryConsumingStock(player, mainHand)) {
            mainHand.setAmount(mainHand.getAmount() + 1);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(@NotNull EntityShootBowEvent event) {
        if (event.getHand() != EquipmentSlot.HAND ||
                !(event.getEntity() instanceof Player player) ||
                !checkPlayerCondition(player, "box.stick.arrow") ||
                !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }

        var bow = event.getBow();

        if (bow == null || bow.getType() != Material.BOW || !event.shouldConsumeItem()) {
            return;
        }

        var arrowItem = event.getConsumable();

        if (arrowItem != null && tryConsumingStock(player, arrowItem)) {
            event.setConsumeItem(false);
            player.updateInventory();

            // If setConsumeItem is set to false, the arrow will not be picked up.
            // This task overwrites it after 1 tick.
            Bukkit.getScheduler().runTask(
                    BoxProvider.get().getPluginInstance(),
                    () -> arrow.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED)
            );
        }
    }

    private boolean tryConsumingStock(@NotNull Player player, @NotNull ItemStack item) {
        var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty()) {
            return false;
        }

        var playerMap = BoxProvider.get().getBoxPlayerMap();

        if (!playerMap.isLoaded(player)) {
            return false;
        }

        var stockHolder = playerMap.get(player).getCurrentStockHolder();

        if (0 < stockHolder.getAmount(boxItem.get())) {
            stockHolder.decrease(boxItem.get());
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPlayerCondition(@NotNull Player player, @NotNull String permissionNode) {
        if (player.getGameMode() != GameMode.ADVENTURE &&
                player.getGameMode() != GameMode.SURVIVAL) {
            return false;
        }

        if (BoxProvider.get().isDisabledWorld(player)) {
            return false;
        }

        if (!player.hasPermission(permissionNode)) {
            return false;
        }

        return boxStickItem.check(player.getInventory().getItemInOffHand());
    }

    private boolean checkChestAccess(@NotNull InventoryView inventoryView) {
        return new InventoryOpenEvent(inventoryView).callEvent();
    }

    private boolean takeResultItem(@NotNull BoxPlayer player, @NotNull FurnaceInventory inventory) {
        var result = inventory.getResult();

        if (result == null) {
            return false;
        }

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(result);

        if (boxItem.isPresent()) {
            player.getCurrentStockHolder().increase(boxItem.get(), result.getAmount());
            inventory.setResult(null);
            playDepositOrWithdrawalSound(player.getPlayer(), true);
            return true;
        } else {
            return false;
        }
    }

    private boolean putFuel(@NotNull BoxPlayer player, @NotNull FurnaceInventory inventory, @NotNull ItemStack mainHand) {
        var currentFuel = inventory.getFuel();

        if (!inventory.isFuel(mainHand) || (currentFuel != null && !currentFuel.isSimilar(mainHand))) {
            return false;
        }

        var fuel = Objects.requireNonNullElse(currentFuel, mainHand);
        var boxItem = BoxProvider.get().getItemManager().getBoxItem(fuel);

        if (boxItem.isEmpty()) {
            return false;
        }

        int currentAmount = currentFuel != null ? currentFuel.getAmount() : 0;
        int maxStackSize = fuel.getType().getMaxStackSize();
        int fuelStock = player.getCurrentStockHolder().getAmount(boxItem.get());
        int newAmount = fuelStock < maxStackSize - currentAmount ? fuelStock + currentAmount : maxStackSize; // This has the same **meaning** as Math.min(fuelStock + currentAmount, 64), but when fuelStock + currentAmount overflows, the way using Math.min causes a bug.
        int consumption = newAmount - currentAmount;

        if (0 < consumption) {
            player.getCurrentStockHolder().decrease(boxItem.get(), consumption);
            inventory.setFuel(fuel.clone().asQuantity(newAmount));
            playDepositOrWithdrawalSound(player.getPlayer(), false);
            return true;
        } else {
            return false;
        }
    }

    private boolean takeResultPotions(@NotNull BoxPlayer player, @NotNull BrewerInventory inventory) {
        boolean result = false;

        // Brewer Inventory (see BrewingStandMenu.java in NMS)
        // 0~2: potion slot | 3: ingredients slot | 4: fuel slot
        for (int i = 0; i < 3; i++) {
            var potion = inventory.getItem(i);

            if (potion == null) {
                continue;
            }

            var boxItem = BoxProvider.get().getItemManager().getBoxItem(potion);

            if (boxItem.isPresent()) {
                player.getCurrentStockHolder().increase(boxItem.get(), potion.getAmount());
                inventory.setItem(i, null);
                playDepositOrWithdrawalSound(player.getPlayer(), true);
                result = true;
            }
        }

        return result;
    }

    private boolean putBlazePowder(@NotNull BoxPlayer player, @NotNull BrewerInventory inventory) {
        var blazePowder = Objects.requireNonNullElseGet(inventory.getFuel(), () -> new ItemStack(Material.BLAZE_POWDER));
        var boxItem = BoxProvider.get().getItemManager().getBoxItem(blazePowder);

        if (boxItem.isEmpty()) {
            return false;
        }

        int currentAmount = inventory.getFuel() != null ? inventory.getFuel().getAmount() : 0;
        int maxStackSize = blazePowder.getType().getMaxStackSize();
        int fuelStock = player.getCurrentStockHolder().getAmount(boxItem.get());
        int newAmount = fuelStock < maxStackSize - currentAmount ? fuelStock + currentAmount : maxStackSize; // This has the same **meaning** as Math.min(fuelStock + currentAmount, 64), but when fuelStock + currentAmount overflows, the way using Math.min causes a bug.
        int consumption = newAmount - currentAmount;

        if (0 < consumption) {
            player.getCurrentStockHolder().decrease(boxItem.get(), consumption);
            inventory.setFuel(blazePowder.clone().asQuantity(newAmount));
            playDepositOrWithdrawalSound(player.getPlayer(), false);
            return true;
        } else {
            return false;
        }
    }

    private boolean isPotion(@NotNull Material material) {
        // see BrewingStandMenu.PotionSlot#mayPlaceItem
        return material == Material.POTION || material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION || material == Material.GLASS_BOTTLE;
    }

    private boolean putPotions(@NotNull BoxPlayer player, @NotNull BrewerInventory inventory, @NotNull ItemStack mainHand) {
        var boxItem = BoxProvider.get().getItemManager().getBoxItem(mainHand);

        if (boxItem.isEmpty()) {
            return false;
        }

        boolean result = false;

        // Brewer Inventory (see BrewingStandMenu.java in NMS)
        // 0~2: potion slot | 3: ingredients slot | 4: fuel slot
        for (int i = 0; i < 3; i++) {
            var potion = inventory.getItem(i);

            if (potion != null) { // if the slot is not empty, ignore it.
                continue;
            }

            player.getCurrentStockHolder().decrease(boxItem.get());
            inventory.setItem(i, boxItem.get().getClonedItem());
            playDepositOrWithdrawalSound(player.getPlayer(), false);
            result = true;
        }

        return result;
    }

    private boolean depositItemsInInventory(@NotNull BoxPlayer player, @NotNull InventoryView inventoryView) {
        var resultList = InventoryTransaction.depositItemsInTopInventory(inventoryView);

        if (resultList.getType().isModified()) {
            resultList.getResultList()
                    .stream()
                    .filter(result -> result.getType().isModified())
                    .forEach(result -> player.getCurrentStockHolder().increase(result.getItem(), result.getAmount()));
            playDepositOrWithdrawalSound(player.getPlayer(), true);
            return true;
        } else {
            return false;
        }
    }

    private boolean withdrawToInventory(@NotNull BoxPlayer player, @NotNull InventoryView inventoryView, @NotNull BoxItem item) {
        var stockHolder = player.getCurrentStockHolder();
        var amount = stockHolder.getAmount(item);

        var result = InventoryTransaction.withdraw(inventoryView, item, amount);

        if (result.getType().isModified()) {
            stockHolder.decrease(result.getItem(), result.getAmount());
            playDepositOrWithdrawalSound(player.getPlayer(), false);
            return true;
        } else {
            return false;
        }
    }

    private void playDepositOrWithdrawalSound(@NotNull Player player, boolean deposit) {
        player.playSound(player.getLocation(), Sound.ENTITY_PIG_SADDLE, 100f, deposit ? 2.0f : 1.5f);
    }

    private static class DummyInventoryView extends InventoryView {

        private final Player player;
        private final Inventory inventory;

        private DummyInventoryView(@NotNull Player player, @NotNull Inventory inventory) {
            this.player = player;
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getTopInventory() {
            return inventory;
        }

        @Override
        public @NotNull Inventory getBottomInventory() {
            return player.getInventory();
        }

        @Override
        public @NotNull HumanEntity getPlayer() {
            return player;
        }

        @Override
        public @NotNull InventoryType getType() {
            return inventory.getType();
        }

        @Override
        public @NotNull String getTitle() {
            return "";
        }
    }
}
