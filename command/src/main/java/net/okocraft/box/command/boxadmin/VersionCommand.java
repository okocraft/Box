package net.okocraft.box.command.boxadmin;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VersionCommand extends AbstractCommand {

    public VersionCommand() {
        super("version", "box.command.admin.version", Set.of("v", "ver"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (checkPermission(sender)) {
            var version = BoxProvider.get().getPluginInstance().getDescription().getVersion();
            sender.sendMessage(BoxAdminMessage.VERSION_INFO.apply(version));
        }
    }
}
