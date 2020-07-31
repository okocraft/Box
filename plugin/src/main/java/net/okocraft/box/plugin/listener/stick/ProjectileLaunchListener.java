package net.okocraft.box.plugin.listener.stick;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.util.PaperChecker;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ProjectileLaunchListener extends AbstractStickListener {

    public ProjectileLaunchListener(@NotNull Box plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(@NotNull ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity().getShooter();

        if (isInDisabledWorld(player)
                || isCreative(player)
                || !hasStick(player, false)
                || !BoxPermission.BOX_STICK_THROW.has(player)
        ) {
            return;
        }

        if (e.getEntity() instanceof EnderPearl) {
            useFromStock(player, Material.ENDER_PEARL);
            return;
        }

        if (e.getEntity() instanceof Egg) {
            useFromStock(player, Material.EGG);
            return;
        }

        if (e.getEntity() instanceof Fireball) {
            useFromStock(player, Material.FIRE_CHARGE);
            return;
        }


        if (e.getEntity() instanceof Firework) {
            useFromStock(player, Material.FIREWORK_ROCKET);
            return;
        }

        if (e.getEntity() instanceof Snowball) {
            useFromStock(player, Material.SNOWBALL);
            return;
        }

        if (e.getEntity() instanceof ThrownExpBottle) {
            useFromStock(player, Material.EXPERIENCE_BOTTLE);
            return;
        }

        if (e.getEntity() instanceof ThrownPotion) {
            useFromStock(player, ((ThrownPotion) e.getEntity()).getItem());
            return;
        }

        if (PaperChecker.isPaper() && e.getEntity() instanceof AbstractArrow) {
            useArrowFromStock(player, ((AbstractArrow) e.getEntity()).getItemStack());
        }
    }

    private void useFromStock(@NotNull Player player, @NotNull Material material) {
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType() != material) {
            return;
        }

        Optional<Item> item = plugin.getItemManager().getItem(hand);

        if (item.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(player.getUniqueId());

        if (user.hasStock(item.get())) {
            plugin.getDataHandler().decrease(user, item.get());
            hand.setAmount(hand.getAmount() + 1);
        }

        // TODO: 残りの所持数表示
    }

    private void useFromStock(@NotNull Player player, @NotNull ItemStack item) {
        Optional<Item> boxItem = plugin.getItemManager().getItem(item);

        if (boxItem.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(player.getUniqueId());

        if (user.hasStock(boxItem.get())) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            plugin.getDataHandler().decrease(user, boxItem.get());
            hand.setAmount(hand.getAmount() + 1);
        }

        // TODO: 残りの所持数表示
    }

    private void useArrowFromStock(@NotNull Player player, @NotNull ItemStack arrow) {
        if (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.ARROW_INFINITE)) {
            return;
        }

        Optional<Item> item = plugin.getItemManager().getItem(arrow);

        if (item.isEmpty()) {
            return;
        }

        User user = plugin.getUserManager().getUser(player.getUniqueId());

        if (user.hasStock(item.get())) {
            ItemStack clonedArrow = arrow.clone();
            clonedArrow.setAmount(1);
            if (player.getInventory().addItem(clonedArrow).isEmpty()) {
                plugin.getDataHandler().decrease(user, item.get());
            } else {
                // TODO: インベ満杯通知
            }
        }

        // TODO: 残りの所持数表示
    }
}
