package net.okocraft.box.feature.command.boxadmin;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VersionCommand extends AbstractCommand {

    private final MessageKey.Arg1<String> versionInfo;
    private final MessageKey help;

    public VersionCommand(@NotNull DefaultMessageCollector collector) {
        super("version", "box.admin.command.version", Set.of("v", "ver"));
        this.versionInfo = MessageKey.arg1(collector.add("box.command.boxadmin.version.info", "<gray>Box version: <aqua><version>"), version -> Argument.string("version", version));
        this.help = MessageKey.key(collector.add("box.command.boxadmin.version.help", "<aqua>/boxadmin version<dark_gray> - <gray>Show the current version of Box"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendMessage(this.versionInfo.apply(this.getClass().getPackage().getImplementationVersion()));
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
