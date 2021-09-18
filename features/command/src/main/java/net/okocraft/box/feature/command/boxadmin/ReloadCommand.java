package net.okocraft.box.feature.command.boxadmin;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        super("reload", "box.admin.command.reload");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendMessage(BoxAdminMessage.RELOAD_START);

        BoxProvider.get().reload(sender);

        sender.sendMessage(BoxAdminMessage.RELOAD_FINISH);
    }
}
