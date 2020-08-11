package net.okocraft.box.plugin.locale.formatter;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.okocraft.box.plugin.util.Colorizer;
import net.okocraft.box.plugin.util.PaperChecker;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FormattedMessage {

    private String message;

    public FormattedMessage(@NotNull String message) {
        this.message = message;
    }

    public void send(@NotNull CommandSender sender) {
        sender.sendMessage(Colorizer.colorize(message));
    }

    public void sendRaw(@NotNull CommandSender sender) {
        sender.sendMessage(message);
    }

    public void showActionBar(@NotNull Player player) {
        String msg = Colorizer.colorize(message);
        if (PaperChecker.isPaper()) {
            player.sendActionBar(msg);
        } else {
            //noinspection deprecation
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
        }
    }

    public void addPrefix(@NotNull String prefix) {
        message = prefix + message;
    }

    @NotNull
    public String asString() {
        return message;
    }
}
