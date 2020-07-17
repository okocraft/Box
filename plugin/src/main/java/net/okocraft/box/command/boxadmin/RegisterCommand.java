package net.okocraft.box.command.boxadmin;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class RegisterCommand extends BaseAdminCommand {

    RegisterCommand() {
        super(
            "register",
            "boxadmin.register",
            1,
            true,
            "/boxadmin register",
            new String[0]
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            messages.sendHoldItem(sender);
            return false;
        }
        itemData.register(item);
        messages.sendItemRegistered(sender, item);

        return true;
    }
}