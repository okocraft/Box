package net.okocraft.box.core.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.scheduler.BoxScheduler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class BoxCommandImpl extends BaseCommand implements BoxCommand {

    private final BoxPlayerMap playerMap;

    private final Predicate<Player> canUseBox;

    public BoxCommandImpl(@NotNull BoxScheduler scheduler, @NotNull BoxPlayerMap playerMap, @NotNull Predicate<Player> canUseBox) {
        super(scheduler);
        this.playerMap = playerMap;
        this.canUseBox = canUseBox;
    }

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
        if (sender instanceof Player player) {
            if (this.playerMap.isLoaded(player)) {
                if (!this.canUseBox.test(player)) {
                    sender.sendMessage(GeneralMessage.ERROR_DISABLED_WORLD.apply(player.getWorld()));
                    return;
                }
            } else {
                if (this.playerMap.isScheduledLoading(player)) {
                    sender.sendMessage(GeneralMessage.ERROR_PLAYER_LOADING);
                } else {
                    sender.sendMessage(GeneralMessage.ERROR_PLAYER_NOT_LOADED);
                }
                return;
            }
        }

        super.onCommand(sender, args);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player) || (this.playerMap.isLoaded(player) && this.canUseBox.test(player))) {
            return super.onTabComplete(sender, args);
        } else {
            return Collections.emptyList();
        }
    }
}
