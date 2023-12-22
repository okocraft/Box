package net.okocraft.box.feature.command.boxadmin;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
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
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

public class ResetAllCommand extends AbstractCommand {

    private static final String CONFIRM = "confirm";
    private static final String CANCEL = "cancel";
    private static final String DEFAULT_CONFIRMATION_MESSAGE = """
            <gray>Reset all data of player <aqua><player_name>
            <red>This operation cannot be undone.
            <gray>To confirm, run <green>/boxadmin resetall confirm
            <gray>To cancel, run <red>/boxadmin resetall cancel
            """;

    private final Map<CommandSender, BoxUser> confirmationMap = new ConcurrentHashMap<>();

    private final Arg1<String> successSender;
    private final Arg1<String> successTarget;
    private final MiniMessageBase cancel;
    private final Arg1<String> confirmation;
    private final MiniMessageBase help;

    public ResetAllCommand(@NotNull DefaultMessageCollector collector) {
        super("resetall", "box.admin.command.resetall");
        this.successSender = arg1(collector.add("box.command.boxadmin.resetall.success.sender", "<aqua><player><gray>'s Box data have been reset."), PLAYER_NAME);
        this.successTarget = arg1(collector.add("box.command.boxadmin.resetall.success.target", "<gray>Your Box data have been reset by <aqua><player_name><gray>."), PLAYER_NAME);
        this.cancel = messageKey(collector.add("box.command.boxadmin.resetall.cancel", "<gray>Cancelled reset operation."));
        this.confirmation = arg1(collector.add("box.command.boxadmin.resetall.confirmation", DEFAULT_CONFIRMATION_MESSAGE), PLAYER_NAME);
        this.help = messageKey(collector.add("box.command.boxadmin.resetall.help", "<aqua>/boxadmin resetall <player><dark_gray> - <gray>Resets player's data"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (args.length < 2) {
            ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
            sender.sendMessage(this.getHelp(msgSrc));
            return;
        }

        if (CONFIRM.equalsIgnoreCase(args[1]) && confirmationMap.containsKey(sender)) {
            var target = confirmationMap.remove(sender);

            BoxAPI.api().getStockManager().getPersonalStockHolder(target).reset();

            BoxAPI.api().getEventManager().call(new UserDataResetEvent(target));

            this.successSender.apply(target.getName().orElseGet(target.getUUID()::toString)).source(msgSrc).send(sender);

            var targetPlayer = Bukkit.getPlayer(target.getUUID());

            if (targetPlayer != null && !sender.getName().equals(targetPlayer.getName())) {
                this.successTarget.apply(sender.getName())
                        .source(BoxAPI.api().getMessageProvider().findSource(targetPlayer))
                        .send(targetPlayer);
            }

            return;
        }

        if (CANCEL.equalsIgnoreCase(args[1]) && confirmationMap.remove(sender) != null) {
            this.cancel.source(msgSrc).send(sender);
            return;
        }

        var target = UserSearcher.search(args[1]);

        if (target != null) {
            confirmationMap.put(sender, target);
            this.confirmation.apply(target.getName().orElseGet(target.getUUID()::toString)).source(msgSrc).send(sender);
        } else {
            ErrorMessages.PLAYER_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }

        if (confirmationMap.containsKey(sender)) {
            var secondArgument = args[1].toLowerCase(Locale.ROOT);

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
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
