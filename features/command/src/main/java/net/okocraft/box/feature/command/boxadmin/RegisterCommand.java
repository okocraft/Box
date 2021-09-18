package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegisterCommand extends AbstractCommand {

    public RegisterCommand() {
        super("register", "box.admin.command.register");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();

        if (mainHandItem.getType().isAir()) {
            player.sendMessage(BoxAdminMessage.REGISTER_IS_AIR);
            return;
        }

        var itemManager = BoxProvider.get().getItemManager();

        if (itemManager.isRegistered(mainHandItem)) {
            player.sendMessage(BoxAdminMessage.REGISTER_ALREADY_REGISTERED.apply(mainHandItem));
            return;
        }

        itemManager.registerCustomItem(mainHandItem)
                .thenAcceptAsync(customItem -> {
                    player.sendMessage(BoxAdminMessage.REGISTER_SUCCESS.apply(customItem));
                    player.sendMessage(BoxAdminMessage.REGISTER_TIP_RENAME);
                })
                .exceptionallyAsync(e -> {
                    player.sendMessage(BoxAdminMessage.REGISTER_FAILURE.apply(e));
                    return null;
                });
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.REGISTER_HELP;
    }
}
