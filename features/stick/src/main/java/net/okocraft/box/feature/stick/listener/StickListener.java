package net.okocraft.box.feature.stick.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.feature.stick.event.stock.StickCause;
import net.okocraft.box.feature.stick.event.stock.StickCauses;
import net.okocraft.box.feature.stick.function.container.BrewerOperator;
import net.okocraft.box.feature.stick.function.container.ChestAccessChecker;
import net.okocraft.box.feature.stick.function.container.ContainerOperation;
import net.okocraft.box.feature.stick.function.container.ContainerOperator;
import net.okocraft.box.feature.stick.function.container.FurnaceOperator;
import net.okocraft.box.feature.stick.function.menu.MenuOpener;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.AbstractArrow;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StickListener implements Listener {

    private final BoxStickItem boxStickItem;

    public StickListener(@NotNull BoxStickItem boxStickItem) {
        this.boxStickItem = boxStickItem;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        var player = event.getPlayer();

        if (event.getAction() == Action.PHYSICAL || isDisabledWorld(player)) {
            return;
        }

        if (event.getAction().isRightClick()) {
            MenuOpener.openMenu(event, boxStickItem);
        } else {
            clickBlock(event);
        }
    }

    private void clickBlock(@NotNull PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        var player = event.getPlayer();
        var boxPlayer = getBoxPlayerOrNull(player);
        var block = event.getClickedBlock();

        if (boxPlayer == null || !player.isSneaking() || block == null) {
            return;
        }

        var inv = player.getInventory();
        var offHand = inv.getItemInOffHand();

        boolean isStickInOffhand = boxStickItem.check(offHand);

        if (isStickInOffhand && block.getState() instanceof Container container) {
            clickContainer(event, boxPlayer, container, block.getLocation().clone());
            return;
        }

        if (MCDataVersion.CURRENT.isBefore(MCDataVersion.MC_1_19_4) || // BlockData#getPlacementMaterial was added in Minecraft 1.19.4
                !player.hasPermission("box.stick.blockitem")) {
            return;
        }

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(block.getBlockData().getPlacementMaterial().name());

        if (boxItem.isEmpty() || boxPlayer.getCurrentStockHolder().getAmount(boxItem.get()) < 1) {
            return;
        }

        var mainHand = inv.getItemInMainHand();
        var cause = new StickCauses.BlockItem(boxPlayer, block.getLocation().clone());

        if (boxStickItem.check(mainHand)) {
            if (!offHand.getType().isAir()) {
                var offHandBoxItem = BoxProvider.get().getItemManager().getBoxItem(offHand);

                if (offHandBoxItem.isEmpty()) {
                    return;
                }

                boxPlayer.getCurrentStockHolder().increase(offHandBoxItem.get(), offHand.getAmount(), cause);
            }

            inv.setItemInOffHand(mainHand.clone()); // Move Box Stick to player's off-hand
        } else if (!isStickInOffhand || !mainHand.getType().isAir()) {
            // The item in player's off-hand is not Box Stick or the main-hand is not empty.
            return;
        }

        boxPlayer.getCurrentStockHolder().decrease(boxItem.get(), 1, cause);
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

        if (inventory instanceof FurnaceInventory furnaceInventory) { // BlastFurnace, Furnace, and Smoker
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
        } else { // other containers (Barrel, Chest, Dispenser, Dropper, Hopper, and ShulkerBox)
            operation = new ContainerOperation<>(
                    ContainerOperation.createContext(boxPlayer, operationType, inventory, clickedBlockLocation),
                    ContainerOperator::process,
                    "container"
            );
        }

        if (!player.hasPermission("box.stick." + operation.permissionSuffix()) ||
                !ChestAccessChecker.canAccess(player, container, operationType)) {
            return;
        }

        if (operation.run() || player.getGameMode() == GameMode.CREATIVE) { // This prevents the instant breaking of blocks in creative mode.
            event.setCancelled(true);
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
        var boxPlayer = checkPlayerAndGetBoxPlayer(player, "box.stick.block");

        if (boxPlayer == null) {
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();

        if (!isIllegalStack(mainHandItem) && event.getItemInHand().equals(mainHandItem) &&
                tryConsumingStock(boxPlayer, mainHandItem, new StickCauses.BlockPlace(boxPlayer, block.getLocation().clone()))) {
            player.getInventory().setItemInMainHand(mainHandItem.clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemConsume(@NotNull PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        var boxPlayer = checkPlayerAndGetBoxPlayer(player, "box.stick.food");

        if (boxPlayer == null) {
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();
        var cause = new StickCauses.ItemConsume(boxPlayer);

        if (isIllegalStack(mainHandItem) || !event.getItem().equals(mainHandItem) || !tryConsumingStock(boxPlayer, mainHandItem, cause)) {
            return;
        }

        event.setReplacement(mainHandItem.clone());

        var defaultReplacementMaterialName = switch (event.getItem().getType()) {
            case MUSHROOM_STEW, RABBIT_STEW, BEETROOT_SOUP, SUSPICIOUS_STEW -> Material.BOWL.name(); // BowlFoodItem#finishUsingItem L15 / SuspiciousStewItem#finishUsingItem L75
            case HONEY_BOTTLE, POTION -> Material.GLASS_BOTTLE.name(); // HoneyBottleItem#finishUsingItem L35 / PotionItem#finishUsingItem L89
            case MILK_BUCKET -> Material.BUCKET.name(); // MilkBucketItem#finishUsingItem L37
            default -> null;
        };

        if (defaultReplacementMaterialName == null) {
            return;
        }

        BoxProvider.get().getItemManager()
                .getBoxItem(defaultReplacementMaterialName)
                .ifPresent(defaultReplacementItem -> boxPlayer.getCurrentStockHolder().increase(defaultReplacementItem, 1, cause));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemBreak(@NotNull PlayerItemBreakEvent event) {
        var player = event.getPlayer();
        var boxPlayer = checkPlayerAndGetBoxPlayer(player, "box.stick.tool");
        var original = event.getBrokenItem();

        if (boxPlayer == null || isIllegalStack(original)) {
            return;
        }

        var copied = original.clone();

        copied.editMeta(Damageable.class, meta -> meta.setDamage(0));

        if (tryConsumingStock(boxPlayer, copied, new StickCauses.ItemBreak(boxPlayer))) {
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
                    case FIREWORK -> "firework";
                    case SNOWBALL -> "snowball";
                    case SPLASH_POTION -> "potion";
                    case THROWN_EXP_BOTTLE -> "expbottle";
                    default -> null;
                };

        var boxPlayer = permissionNodeSuffix != null ? checkPlayerAndGetBoxPlayer(player, "box.stick." + permissionNodeSuffix) : null;

        if (boxPlayer == null) {
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();

        if (!isIllegalStack(mainHandItem) && tryConsumingStock(boxPlayer, mainHandItem, new StickCauses.ProjectileLaunch(boxPlayer, entityType))) {
            mainHandItem.setAmount(mainHandItem.getAmount() + 1);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(@NotNull EntityShootBowEvent event) {
        if (event.getHand() != EquipmentSlot.HAND ||
                !(event.getEntity() instanceof Player player) || !(event.getProjectile() instanceof Arrow arrow) ||
                event.getBow() == null || event.getBow().getType() != Material.BOW ||
                !event.shouldConsumeItem()) {
            return;
        }

        var boxPlayer = checkPlayerAndGetBoxPlayer(player, "box.stick.arrow");

        if (boxPlayer == null) {
            return;
        }

        var arrowItem = event.getConsumable();

        if (arrowItem != null && !isIllegalStack(arrowItem) && tryConsumingStock(boxPlayer, arrowItem, new StickCauses.ShootBow(boxPlayer))) {
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

    private @Nullable BoxPlayer getBoxPlayerOrNull(@NotNull Player player) {
        var playerMap = BoxProvider.get().getBoxPlayerMap();
        return playerMap.isLoaded(player) ? playerMap.get(player) : null;
    }

    private boolean tryConsumingStock(@NotNull BoxPlayer boxPlayer, @NotNull ItemStack item, @NotNull StickCause cause) {
        var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty()) {
            return false;
        }

        var stockHolder = boxPlayer.getCurrentStockHolder();

        if (0 < stockHolder.getAmount(boxItem.get())) {
            stockHolder.decrease(boxItem.get(), 1, cause);
            return true;
        } else {
            return false;
        }
    }

    private @Nullable BoxPlayer checkPlayerAndGetBoxPlayer(@NotNull Player player, @NotNull String permissionNode) {
        if (isSurvivalOrAdventure(player) && !isDisabledWorld(player) &&
                player.hasPermission(permissionNode) && hasBoxStickInOffHand(player)) {
            return getBoxPlayerOrNull(player);
        } else {
            return null;
        }
    }

    private boolean isDisabledWorld(@NotNull Player player) {
        return BoxProvider.get().isDisabledWorld(player);
    }

    private boolean isSurvivalOrAdventure(@NotNull Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }

    private boolean hasBoxStickInOffHand(@NotNull Player player) {
        return boxStickItem.check(player.getInventory().getItemInOffHand());
    }

    private boolean isIllegalStack(@NotNull ItemStack itemStack) {
        return itemStack.getMaxStackSize() < itemStack.getAmount();
    }
}
