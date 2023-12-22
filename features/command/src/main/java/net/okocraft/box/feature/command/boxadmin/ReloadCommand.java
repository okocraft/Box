package net.okocraft.box.feature.command.boxadmin;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class ReloadCommand extends AbstractCommand {

    private final MiniMessageBase start;
    private final MiniMessageBase finish;
    private final MiniMessageBase help;

    public ReloadCommand(@NotNull DefaultMessageCollector collector) {
        super("reload", "box.admin.command.reload");
        this.start = messageKey(collector.add("box.command.boxadmin.reload.start", "<gray>Reloading Box..."));
        this.finish = messageKey(collector.add("box.command.boxadmin.reload.finish", "<gray>Box has been reloaded!"));
        this.help = messageKey(collector.add("box.command.boxadmin.reload.help", "<aqua>/boxadmin reload<dark_gray> - <gray>Reloads Box"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);
        this.start.source(msgSrc).send(sender);

        BoxAPI.api().reload(sender);

        this.finish.source(msgSrc).send(sender);
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
