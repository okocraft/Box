package net.okocraft.box.feature.stick.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.player.BoxPlayer;
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

        if (boxPlayer == null || !player.isSneaking() || !boxStickItem.check(player.getInventory().getItemInOffHand())) {
            return;
        }

        var block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (block.getState() instanceof Container container) {
            clickContainer(event, boxPlayer, container, block.getLocation().clone());
        }
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
            operation = new ContainerOperation<>(boxPlayer, "furnace", operationType, furnaceInventory, FurnaceOperator::process, clickedBlockLocation);
        } else if (inventory instanceof BrewerInventory brewerInventory) { // BrewingStand
            operation = new ContainerOperation<>(boxPlayer, "brewer", operationType, brewerInventory, BrewerOperator::process, clickedBlockLocation);
        } else { // other containers (Barrel, Chest, Dispenser, Dropper, Hopper, and ShulkerBox)
            operation = new ContainerOperation<>(boxPlayer, "container", operationType, inventory, ContainerOperator::process, clickedBlockLocation);
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

        // ignore POWDER_SNOW because it cannot be replenished
        if (event.getBlockPlaced().getType() == Material.POWDER_SNOW) {
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

        if (event.getItemInHand().equals(mainHandItem) && tryConsumingStock(boxPlayer, mainHandItem)) {
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

        if (!event.getItem().equals(mainHandItem) || !tryConsumingStock(boxPlayer, mainHandItem)) {
            return;
        }

        event.setReplacement(mainHandItem.clone());

        if (mainHandItem.getAmount() == 1) {
            var defaultReplacementMaterialName = switch (event.getItem().getType()) {
                case MUSHROOM_STEW, RABBIT_STEW, BEETROOT_SOUP, SUSPICIOUS_STEW -> Material.BOWL.name();
                case HONEY_BOTTLE, POTION -> Material.GLASS_BOTTLE.name();
                case MILK_BUCKET -> Material.BUCKET.name();
                default -> null;
            };

            if (defaultReplacementMaterialName == null) {
                return;
            }

            BoxProvider.get().getItemManager()
                    .getBoxItem(defaultReplacementMaterialName)
                    .ifPresent(defaultReplacementItem -> boxPlayer.getCurrentStockHolder().increase(defaultReplacementItem, 1));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemBreak(@NotNull PlayerItemBreakEvent event) {
        var player = event.getPlayer();
        var boxPlayer = checkPlayerAndGetBoxPlayer(player, "box.stick.tool");

        if (boxPlayer == null) {
            return;
        }

        var original = event.getBrokenItem();
        var copied = original.clone();

        copied.editMeta(Damageable.class, meta -> meta.setDamage(0));

        if (tryConsumingStock(boxPlayer, copied)) {
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

        var boxPlayer = permissionNodeSuffix != null ? checkPlayerAndGetBoxPlayer(player, permissionNodeSuffix) : null;

        if (boxPlayer == null) {
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();

        if (tryConsumingStock(boxPlayer, mainHandItem)) {
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

        if (arrowItem != null && tryConsumingStock(boxPlayer, arrowItem)) {
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

    private boolean tryConsumingStock(@NotNull BoxPlayer boxPlayer, @NotNull ItemStack item) {
        var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty()) {
            return false;
        }

        var stockHolder = boxPlayer.getCurrentStockHolder();

        if (0 < stockHolder.getAmount(boxItem.get())) {
            stockHolder.decrease(boxItem.get(), 1);
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
}
