package net.okocraft.box.feature.stick.listener;

import com.github.siroshun09.configapi.api.value.ConfigValue;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.stick.integration.LWCIntegration;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.block.Lockable;
import org.bukkit.block.ShulkerBox;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

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

        if (block == null || !canDepositOrWithdraw(block.getState())) {
            return;
        }

        var state = (Container) block.getState();

        var view = new DummyInventoryView(player, state.getInventory());

        // for WorldGuard (flag: chest-access)
        if (!checkChestAccess(view)) {
            return;
        }

        event.setCancelled(true);

        var deposit = mainHand.getType().isAir();

        if (!LWCIntegration.canModifyInventory(player, state, deposit)) {
            return;
        }

        if (deposit) {
            depositItemsInInventory(player, view);
        } else {
            BoxProvider.get()
                    .getItemManager()
                    .getBoxItem(mainHand)
                    .ifPresent(item -> withdrawToInventory(player, view, item));
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

        copied.editMeta(meta -> {
            if (meta instanceof Damageable damageable) {
                damageable.setDamage(0);
            }
        });

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

    private boolean canDepositOrWithdraw(@NotNull BlockState state) {
        return !(state instanceof Lockable lockable && lockable.isLocked()) &&
                (state instanceof Barrel || state instanceof Chest ||
                        state instanceof Dispenser || state instanceof Dropper ||
                        state instanceof Hopper || state instanceof ShulkerBox);
    }

    private void depositItemsInInventory(@NotNull Player player, @NotNull InventoryView inventoryView) {
        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();
        var resultList = InventoryTransaction.depositItemsInTopInventory(inventoryView);

        if (resultList.getType().isModified()) {
            resultList.getResultList()
                    .stream()
                    .filter(result -> result.getType().isModified())
                    .forEach(result -> stockHolder.increase(result.getItem(), result.getAmount()));
            player.playSound(player.getLocation(), Sound.ENTITY_PIG_SADDLE, 100f, 2.0f);
        }
    }

    private void withdrawToInventory(@NotNull Player player, @NotNull InventoryView inventoryView, @NotNull BoxItem item) {
        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();

        var amount = stockHolder.getAmount(item);

        var result = InventoryTransaction.withdraw(inventoryView, item, amount);

        if (result.getType().isModified()) {
            stockHolder.decrease(result.getItem(), result.getAmount());
            player.playSound(player.getLocation(), Sound.ENTITY_PIG_SADDLE, 100f, 1.5f);
        }
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
