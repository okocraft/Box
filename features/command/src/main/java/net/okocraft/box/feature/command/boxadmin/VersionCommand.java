package net.okocraft.box.feature.command.boxadmin;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VersionCommand extends AbstractCommand {

    private final Arg1<String> versionInfo;
    private final MiniMessageBase help;

    public VersionCommand(@NotNull DefaultMessageCollector collector) {
        super("version", "box.admin.command.version", Set.of("v", "ver"));
        this.versionInfo = Arg1.arg1(collector.add("box.command.boxadmin.version.info", "<gray>Box version: <aqua><version>"), Placeholder.component("version", Component::text));
        this.help = MiniMessageBase.messageKey(collector.add("box.command.boxadmin.version.help", "<aqua>/boxadmin version<dark_gray> - <gray>Show the current version of Box"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        this.versionInfo.apply(this.getClass().getPackage().getImplementationVersion())
            .source(BoxAPI.api().getMessageProvider().findSource(sender))
            .send(sender);
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
