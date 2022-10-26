package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VersionCommand extends AbstractCommand {

    public VersionCommand() {
        super("version", "box.admin.command.version", Set.of("v", "ver"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var version = getClass().getPackage().getImplementationVersion();
        sender.sendMessage(BoxAdminMessage.VERSION_INFO.apply(version));
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.VERSION_HELP;
    }
}
