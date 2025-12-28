package net.okocraft.box.feature.command.boxadmin;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand extends AbstractCommand {

    private final MessageKey start;
    private final MessageKey finish;
    private final MessageKey help;

    public ReloadCommand(@NotNull DefaultMessageCollector collector) {
        super("reload", "box.admin.command.reload");
        this.start = MessageKey.key(collector.add("box.command.boxadmin.reload.start", "<gray>Reloading Box..."));
        this.finish = MessageKey.key(collector.add("box.command.boxadmin.reload.finish", "<gray>Box has been reloaded!"));
        this.help = MessageKey.key(collector.add("box.command.boxadmin.reload.help", "<aqua>/boxadmin reload<dark_gray> - <gray>Reloads Box"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendMessage(this.start);
        BoxAPI.api().reload(sender);
        sender.sendMessage(this.finish);
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
