package net.okocraft.box.feature.command.boxadmin;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.event.user.UserDataResetEvent;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

public class ResetAllCommand extends AbstractCommand {

    private static final String CONFIRM = "confirm";
    private static final String CANCEL = "cancel";
    private static final String DEFAULT_CONFIRMATION_MESSAGE = """
        <gray>Reset all data of player <aqua><player_name>
        <red>This operation cannot be undone.
        <gray>To confirm, run <green>/boxadmin resetall confirm
        <gray>To cancel, run <red>/boxadmin resetall cancel""";

    private final Map<CommandSender, BoxUser> confirmationMap = new ConcurrentHashMap<>();

    private final MessageKey.Arg1<String> successSender;
    private final MessageKey.Arg1<String> successTarget;
    private final MessageKey cancel;
    private final MessageKey.Arg1<String> confirmation;
    private final MessageKey help;

    public ResetAllCommand(@NotNull DefaultMessageCollector collector) {
        super("resetall", "box.admin.command.resetall");
        this.successSender = MessageKey.arg1(collector.add("box.command.boxadmin.resetall.success.sender", "<aqua><player><gray>'s Box data have been reset."), PLAYER_NAME);
        this.successTarget = MessageKey.arg1(collector.add("box.command.boxadmin.resetall.success.target", "<gray>Your Box data have been reset by <aqua><player_name><gray>."), PLAYER_NAME);
        this.cancel = MessageKey.key(collector.add("box.command.boxadmin.resetall.cancel", "<gray>Cancelled reset operation."));
        this.confirmation = MessageKey.arg1(collector.add("box.command.boxadmin.resetall.confirmation", DEFAULT_CONFIRMATION_MESSAGE), PLAYER_NAME);
        this.help = MessageKey.key(collector.add("box.command.boxadmin.resetall.help", "<aqua>/boxadmin resetall <player><dark_gray> - <gray>Resets player's data"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        if (CONFIRM.equalsIgnoreCase(args[1]) && this.confirmationMap.containsKey(sender)) {
            BoxUser target = this.confirmationMap.remove(sender);

            BoxAPI.api().getStockManager().getPersonalStockHolder(target).reset();

            BoxAPI.api().getEventCallers().sync().call(new UserDataResetEvent(target));

            sender.sendMessage(this.successSender.apply(target.getName().orElseGet(target.getUUID()::toString)));

            Player targetPlayer = Bukkit.getPlayer(target.getUUID());

            if (targetPlayer != null && !sender.getName().equals(targetPlayer.getName())) {
                targetPlayer.sendMessage(this.successTarget.apply(sender.getName()));
            }

            return;
        }

        if (CANCEL.equalsIgnoreCase(args[1]) && this.confirmationMap.remove(sender) != null) {
            sender.sendMessage(this.cancel);
            return;
        }

        BoxUser target = UserSearcher.search(args[1]);

        if (target != null) {
            this.confirmationMap.put(sender, target);
            sender.sendMessage(this.confirmation.apply(target.getName().orElseGet(target.getUUID()::toString)));
        } else {
            sender.sendMessage(ErrorMessages.PLAYER_NOT_FOUND.apply(args[1]));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }

        if (this.confirmationMap.containsKey(sender)) {
            String secondArgument = args[1].toLowerCase(Locale.ROOT);

            if (secondArgument.isEmpty() ||
                (secondArgument.length() == 1 && secondArgument.charAt(0) == 'c')) {
                return List.of(CONFIRM, CANCEL);
            }

            if (secondArgument.length() < 7 && secondArgument.startsWith("ca")) {
                return List.of(CANCEL);
            }

            if (secondArgument.length() < 8 && secondArgument.startsWith("co")) {
                return List.of(CONFIRM);
            }

            return Collections.emptyList();
        } else {
            return TabCompleter.players(args[1]);
        }
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
