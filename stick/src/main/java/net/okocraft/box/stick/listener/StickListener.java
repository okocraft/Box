package net.okocraft.box.stick.listener;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.stick.item.BoxStickItem;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class StickListener implements Listener {

    private final BoxStickItem boxStickItem;

    public StickListener(@NotNull BoxStickItem boxStickItem) {
        this.boxStickItem = boxStickItem;
    }

    @EventHandler
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

    @EventHandler
    public void onItemConsume(@NotNull PlayerItemConsumeEvent event) {
        var player = event.getPlayer();

        if (!player.hasPermission("box.stick.food")) {
            return;
        }

        if (tryConsumingStock(player, event.getItem())) {
            event.setItem(event.getItem().clone());
        }
    }

    @EventHandler
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

    @EventHandler
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
                mainHand.getType() == Material.LINGERING_POTION) {
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

        if (BoxProvider.get().isDisabledWorld(player.getWorld())) {
            return false;
        }

        return boxStickItem.check(player.getInventory().getItemInOffHand());
    }
}
