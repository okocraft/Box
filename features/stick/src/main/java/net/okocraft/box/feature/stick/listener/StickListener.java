package net.okocraft.box.feature.stick.listener;

import com.github.siroshun09.configapi.api.value.ConfigValue;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
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

        if (event.getHand() == EquipmentSlot.OFF_HAND ||
                event.getAction() == Action.PHYSICAL ||
                !player.hasPermission("box.stick.menu")) {
            return;
        }

        if (BoxProvider.get().isDisabledWorld(player) ||
                !boxStickItem.check(player.getInventory().getItemInMainHand())) {
            return;
        }

        var command = BoxProvider.get().getConfiguration().get(MENU_COMMAND_SETTING);

        if (!command.isEmpty()) {
            Bukkit.dispatchCommand(player, command);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        var player = event.getPlayer();

        if (!player.hasPermission("box.stick.block")) {
            return;
        }

        var inHand = event.getItemInHand();
        var mainHandItem = player.getInventory().getItemInMainHand();

        if (!inHand.isSimilar(mainHandItem)) {
            return;
        }

        if (tryConsumingStock(player, mainHandItem)) {
            mainHandItem.setAmount(mainHandItem.getAmount());
            player.getInventory().setItemInMainHand(mainHandItem);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemConsume(@NotNull PlayerItemConsumeEvent event) {
        var player = event.getPlayer();

        if (!player.hasPermission("box.stick.food")) {
            return;
        }

        if (tryConsumingStock(player, event.getItem())) {
            event.setItem(event.getItem().clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemBreak(@NotNull PlayerItemBreakEvent event) {
        var player = event.getPlayer();

        if (!player.hasPermission("box.stick.tool")) {
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
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1f, 1f);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionThrow(@NotNull ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion potion)) {
            return;
        }

        if (!(potion.getShooter() instanceof Player player)) {
            return;
        }

        if (!player.hasPermission("box.stick.potion")) {
            return;
        }

        var mainHand = player.getInventory().getItemInMainHand();

        if (mainHand.getType() != Material.SPLASH_POTION &&
                mainHand.getType() != Material.LINGERING_POTION) {
            return;
        }

        if (tryConsumingStock(player, mainHand)) {
            mainHand.setAmount(mainHand.getAmount() + 1);
            player.getInventory().setItemInMainHand(mainHand);
        }
    }

    private boolean tryConsumingStock(@NotNull Player player, @NotNull ItemStack item) {
        if (!checkPlayerCondition(player)) {
            return false;
        }

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty()) {
            return false;
        }

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();

        if (0 < stockHolder.getAmount(boxItem.get())) {
            stockHolder.decrease(boxItem.get());
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPlayerCondition(@NotNull Player player) {
        if (player.getGameMode() != GameMode.ADVENTURE &&
                player.getGameMode() != GameMode.SURVIVAL) {
            return false;
        }

        if (BoxProvider.get().isDisabledWorld(player)) {
            return false;
        }

        return boxStickItem.check(player.getInventory().getItemInOffHand());
    }
}
