package net.okocraft.box.core.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.api.message.GeneralMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class BoxCommandImpl extends BaseCommand implements BoxCommand {

    @Override
    public @NotNull String getName() {
        return "box";
    }

    @Override
    public @NotNull String getPermissionNode() {
        return "box.command";
    }

    @Override
    public @NotNull @Unmodifiable Set<String> getAliases() {
        return Set.of("b", "okobox");
    }

    @Override
    public @NotNull Component getHelp() {
        return Components.commandHelp("box.command.box");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player && !BoxProvider.get().getBoxPlayerMap().isLoaded(player)) {
            if (BoxProvider.get().getBoxPlayerMap().isScheduledLoading(player)) {
                sender.sendMessage(GeneralMessage.ERROR_PLAYER_LOADING);
            } else {
                sender.sendMessage(GeneralMessage.ERROR_PLAYER_NOT_LOADED);
            }
            return;
        }

        super.onCommand(sender, args);
    }
}
