package net.okocraft.box.feature.stick.listener;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.feature.stick.event.stock.StickCause;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import net.okocraft.box.feature.stick.function.container.BrewerOperator;
import net.okocraft.box.feature.stick.function.container.ChestAccessChecker;
import net.okocraft.box.feature.stick.function.container.ContainerOperation;
import net.okocraft.box.feature.stick.function.container.ContainerOperator;
import net.okocraft.box.feature.stick.function.container.FurnaceOperator;
import net.okocraft.box.feature.stick.integration.CoreProtectIntegration;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class StickListener implements Listener {

    private static final EnumSet<InventoryType> CONTAINERS = EnumSet.of(InventoryType.BARREL, InventoryType.CHEST, InventoryType.DISPENSER, InventoryType.DROPPER, InventoryType.HOPPER, InventoryType.SHULKER_BOX);

    private final BoxStickItem boxStickItem;

    public StickListener(@NotNull BoxStickItem boxStickItem) {
        this.boxStickItem = boxStickItem;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (event.getAction() == Action.PHYSICAL || !this.canUseBox(player)) {
            return;
        }

        if (event.getAction().isRightClick()) {
            this.onRightClick(event);
        } else {
            this.clickBlock(event);
        }
    }

    private void onRightClick(@NotNull PlayerInteractEvent event) {
        var player = event.getPlayer();

        boolean doAction;

        if (event.getHand() == EquipmentSlot.HAND) {
            doAction = this.boxStickItem.check(player.getInventory().getItemInMainHand());
        } else { // OFF_HAND
            doAction = player.getInventory().getItemInMainHand().getType().isAir() && this.boxStickItem.check(player.getInventory().getItemInOffHand());
        }

        if (doAction) {
            this.boxStickItem.onRightClick(player);
        }
    }

    private void clickBlock(@NotNull PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        var player = event.getPlayer();
        var boxPlayer = this.getBoxPlayerOrNull(player);
        var block = event.getClickedBlock();

        if (boxPlayer == null || !player.isSneaking() || block == null) {
            return;
        }

        var inv = player.getInventory();
        var offHand = inv.getItemInOffHand();

        boolean isStickInOffhand = this.boxStickItem.check(offHand);

        if (isStickInOffhand && block.getState() instanceof Container container) {
            this.clickContainer(event, boxPlayer, container, block.getLocation().clone());
            return;
        }

        if (!player.hasPermission("box.stick.blockitem")) {
            return;
        }

        var boxItem = BoxAPI.api().getItemManager().getBoxItem(block.getBlockData().getPlacementMaterial().name());

        if (boxItem.isEmpty()) {
            return;
        }

        var mainHand = inv.getItemInMainHand();
        BoxItem offHandBoxItem;
        boolean moveStickToOffHand;

        if (this.boxStickItem.check(mainHand)) {
            if (!offHand.getType().isAir()) {
                var optionalOffHandBoxItem = BoxAPI.api().getItemManager().getBoxItem(offHand);

                if (optionalOffHandBoxItem.isEmpty()) {
                    return;
                }

                offHandBoxItem = optionalOffHandBoxItem.get();
            } else {
                offHandBoxItem = null;
            }

            moveStickToOffHand = true;
        } else if (!isStickInOffhand || !mainHand.getType().isAir()) {
            // The item in player's off-hand is not Box Stick or the main-hand is not empty.
            return;
        } else {
            offHandBoxItem = null;
            moveStickToOffHand = false;
        }

        var cause = new StickCauses.BlockItem(boxPlayer, block.getLocation().clone());

        if (boxPlayer.getCurrentStockHolder().decreaseIfPossible(boxItem.get(), 1, cause) == -1) {
            return;
        }

        if (moveStickToOffHand) {
            if (offHandBoxItem != null) {
                boxPlayer.getCurrentStockHolder().increase(offHandBoxItem, offHand.getAmount(), cause);
            }
            inv.setItemInOffHand(mainHand.clone());
        }

        inv.setItemInMainHand(boxItem.get().getOriginal().asOne());
        event.setCancelled(true);
    }

    private void clickContainer(@NotNull PlayerInteractEvent event, @NotNull BoxPlayer boxPlayer, @NotNull Container container, @NotNull Location clickedBlockLocation) {
        if (container.isLocked()) {
            return;
        }

        var player = boxPlayer.getPlayer();

        var operationType = player.getInventory().getItemInMainHand().getType().isAir() ? ContainerOperation.OperationType.DEPOSIT : ContainerOperation.OperationType.WITHDRAW;
        var inventory = container.getInventory();
        ContainerOperation<?> operation;

        if (CONTAINERS.contains(inventory.getType())) {
            operation = new ContainerOperation<>(
                ContainerOperation.createContext(boxPlayer, operationType, inventory, clickedBlockLocation),
                ContainerOperator::process,
                "container"
            );
        } else if (inventory instanceof FurnaceInventory furnaceInventory) { // BlastFurnace, Furnace, and Smoker
            operation = new ContainerOperation<>(
                ContainerOperation.createContext(boxPlayer, operationType, furnaceInventory, clickedBlockLocation),
                FurnaceOperator::process,
                "furnace"
            );
        } else if (inventory instanceof BrewerInventory brewerInventory) { // BrewingStand
            operation = new ContainerOperation<>(
                ContainerOperation.createContext(boxPlayer, operationType, brewerInventory, clickedBlockLocation),
                BrewerOperator::process,
                "brewer"
            );
        } else {
            return;
        }

        if (!player.hasPermission("box.stick." + operation.permissionSuffix()) ||
            !ChestAccessChecker.canAccess(player, container, operationType)) {
            return;
        }

        try {
            CoreProtectIntegration.logContainerTransaction(player, container);
            if (operation.run() || player.getGameMode() == GameMode.CREATIVE) { // This prevents the instant breaking of blocks in creative mode.
                event.setCancelled(true);
            }
        } finally {
            CoreProtectIntegration.logContainerTransaction(player, container);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        var block = event.getBlockPlaced();

        // ignore POWDER_SNOW because it cannot be replenished
        if (block.getType() == Material.POWDER_SNOW) {
            return;
        }

        // ignore tools (flint and steel, axes, hoes, and shovels)
        if (0 < event.getItemInHand().getType().getMaxDurability()) {
            return;
        }

        var player = event.getPlayer();
        var boxPlayer = this.checkPlayerAndGetBoxPlayer(player, "box.stick.block");

        if (boxPlayer == null) {
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();

        if (!this.isIllegalStack(mainHandItem) && event.getItemInHand().equals(mainHandItem) &&
            this.tryConsumingStock(boxPlayer, mainHandItem, new StickCauses.BlockPlace(boxPlayer, block.getLocation().clone()))) {
            player.getInventory().setItemInMainHand(mainHandItem.clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemConsume(@NotNull PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        var boxPlayer = this.checkPlayerAndGetBoxPlayer(player, "box.stick.food");

        if (boxPlayer == null) {
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();
        var cause = new StickCauses.ItemConsume(boxPlayer);

        if (this.isIllegalStack(mainHandItem) || !event.getItem().equals(mainHandItem) || !this.tryConsumingStock(boxPlayer, mainHandItem, cause)) {
            return;
        }

        event.setReplacement(mainHandItem.clone());

        // @formatter:off
        var defaultReplacementMaterial = switch (event.getItem().getType()) {
            case MUSHROOM_STEW, RABBIT_STEW, BEETROOT_SOUP, SUSPICIOUS_STEW -> Material.BOWL; // BowlFoodItem#finishUsingItem L15 / SuspiciousStewItem#finishUsingItem L75
            case HONEY_BOTTLE, POTION -> Material.GLASS_BOTTLE; // HoneyBottleItem#finishUsingItem L35 / PotionItem#finishUsingItem L89
            case MILK_BUCKET -> Material.BUCKET; // MilkBucketItem#finishUsingItem L37
            default -> null;
        };
        // @formatter:on

        if (defaultReplacementMaterial == null) {
            return;
        }

        BoxAPI.api().getItemManager()
            .getBoxItem(ItemNameGenerator.key(defaultReplacementMaterial))
            .ifPresent(defaultReplacementItem -> boxPlayer.getCurrentStockHolder().increase(defaultReplacementItem, 1, cause));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemBreak(@NotNull PlayerItemBreakEvent event) {
        var player = event.getPlayer();
        var boxPlayer = this.checkPlayerAndGetBoxPlayer(player, "box.stick.tool");
        var original = event.getBrokenItem();

        if (boxPlayer == null || this.isIllegalStack(original)) {
            return;
        }

        original.setData(DataComponentTypes.DAMAGE, 0);

        if (this.tryConsumingStock(boxPlayer, original, new StickCauses.ItemBreak(boxPlayer))) {
            original.setAmount(2);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileLaunch(@NotNull ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        var entityType = event.getEntity().getType();
        var permissionNodeSuffix =
            switch (entityType) {
                case EGG -> "egg";
                case ENDER_PEARL -> "enderpearl";
                case FIREWORK_ROCKET -> "firework";
                case SNOWBALL -> "snowball";
                case POTION -> "potion";
                case EXPERIENCE_BOTTLE -> "expbottle";
                case WIND_CHARGE -> "wind_charge";
                default -> null;
            };

        var boxPlayer = permissionNodeSuffix != null ? this.checkPlayerAndGetBoxPlayer(player, "box.stick." + permissionNodeSuffix) : null;

        if (boxPlayer == null) {
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();

        if (!this.isIllegalStack(mainHandItem) && this.tryConsumingStock(boxPlayer, mainHandItem, new StickCauses.ProjectileLaunch(boxPlayer, entityType))) {
            mainHandItem.setAmount(mainHandItem.getAmount() + 1);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(@NotNull EntityShootBowEvent event) {
        if (event.getHand() != EquipmentSlot.HAND ||
            !(event.getEntity() instanceof Player player) || !(event.getProjectile() instanceof Arrow) ||
            event.getBow() == null || event.getBow().getType() != Material.BOW || event.getBow().containsEnchantment(Enchantment.INFINITY) ||
            !event.shouldConsumeItem()) {
            return;
        }

        var boxPlayer = this.checkPlayerAndGetBoxPlayer(player, "box.stick.arrow");

        if (boxPlayer == null) {
            return;
        }

        var arrowItem = event.getConsumable();

        if (arrowItem != null && !this.isIllegalStack(arrowItem) && this.tryConsumingStock(boxPlayer, arrowItem, new StickCauses.ShootBow(boxPlayer))) {
            player.getInventory().addItem(arrowItem.asOne());
        }
    }

    private @Nullable BoxPlayer getBoxPlayerOrNull(@NotNull Player player) {
        var playerMap = BoxAPI.api().getBoxPlayerMap();
        return playerMap.isLoaded(player) ? playerMap.get(player) : null;
    }

    private boolean tryConsumingStock(@NotNull BoxPlayer boxPlayer, @NotNull ItemStack item, @NotNull StickCause cause) {
        var boxItem = BoxAPI.api().getItemManager().getBoxItem(item);
        return boxItem.isPresent() && boxPlayer.getCurrentStockHolder().decreaseIfPossible(boxItem.get(), 1, cause) != -1;
    }

    private @Nullable BoxPlayer checkPlayerAndGetBoxPlayer(@NotNull Player player, @NotNull String permissionNode) {
        if (this.isSurvivalOrAdventure(player) && this.canUseBox(player) &&
            player.hasPermission(permissionNode) && this.hasBoxStickInOffHand(player)) {
            return this.getBoxPlayerOrNull(player);
        } else {
            return null;
        }
    }

    private boolean canUseBox(@NotNull Player player) {
        return BoxAPI.api().canUseBox(player);
    }

    private boolean isSurvivalOrAdventure(@NotNull Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }

    private boolean hasBoxStickInOffHand(@NotNull Player player) {
        return this.boxStickItem.check(player.getInventory().getItemInOffHand());
    }

    private boolean isIllegalStack(@NotNull ItemStack itemStack) {
        return itemStack.getMaxStackSize() < itemStack.getAmount();
    }
}
