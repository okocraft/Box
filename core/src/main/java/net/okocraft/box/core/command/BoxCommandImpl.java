package net.okocraft.box.core.command;

import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.message.MessageProvider;
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

    public BoxCommandImpl(@NotNull MessageProvider messageProvider, @NotNull BoxScheduler scheduler,
                          @NotNull BoxPlayerMap playerMap, @NotNull Predicate<Player> canUseBox) {
        super(messageProvider, scheduler);
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
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (this.playerMap.isLoaded(player)) {
                if (!this.canUseBox.test(player)) {
                    ErrorMessages.CANNOT_USE_BOX.source(this.messageProvider.findSource(sender)).send(sender);
                    return;
                }
            } else {
                if (this.playerMap.isScheduledLoading(player)) {
                    ErrorMessages.playerDataIsLoading(null).source(this.messageProvider.findSource(sender)).send(sender);
                } else {
                    ErrorMessages.playerDataIsNotLoaded(null).source(this.messageProvider.findSource(sender)).send(sender);
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
