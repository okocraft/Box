package net.okocraft.box.command.boxadmin;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        super("reload", "box.command.admin.reload");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender)) {
            return;
        }

        sender.sendMessage(BoxAdminMessage.RELOAD_START);

        BoxProvider.get().reload(sender);

        sender.sendMessage(BoxAdminMessage.RELOAD_FINISH);
    }
}
