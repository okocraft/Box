package net.okocraft.box.command.boxadmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.gui.GUICache;

class CustomNameCommand extends BaseAdminCommand {

    CustomNameCommand() {
        super(
            "customname",
            "boxadmin.customname",
            2,
            false,
            "/boxadmin customname <<before> <after> | <after>>",
            new String[0]
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        ItemStack modify;
        String customName;
        if (args.length == 2 && sender instanceof Player) {
            modify = ((Player) sender).getInventory().getItemInMainHand();
            customName = args[1];
        } else if (args.length >= 3) {
            modify = itemData.getItemStack(args[1].toUpperCase(Locale.ROOT));
            customName = args[2];
        } else {
            messages.sendHoldItem(sender);
            return false;
        }

        String currentName = itemData.getName(modify);
        if (currentName == null) {
            messages.sendItemNotFound(sender);
            return false;
        }
        itemData.setCustomName(modify, ChatColor.translateAlternateColorCodes('&', customName).toUpperCase(Locale.ROOT));
        String newName = itemData.getName(modify);
        messages.sendItemNameChanged(sender, currentName, newName);
        
        categories.replaceItem(currentName, newName);

        // recache.
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.closeInventory();            
            GUICache.removeCache(player);
        });

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (sender instanceof Player && itemData.getName(((Player) sender).getInventory().getItemInMainHand()) != null) {
                return StringUtil.copyPartialMatches(args[1], List.of("<after>"), new ArrayList<>());
            }
            return StringUtil.copyPartialMatches(args[1], itemData.getNames(), new ArrayList<>());
        }

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], List.of("<after>"), new ArrayList<>());
        }

        return List.of();
    }
}