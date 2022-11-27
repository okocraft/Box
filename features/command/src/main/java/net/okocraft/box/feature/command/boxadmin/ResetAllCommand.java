package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.event.user.UserDataResetEvent;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserStockHolderOperator;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResetAllCommand extends AbstractCommand {

    private static final String CONFIRM = "confirm";
    private static final String CANCEL = "cancel";

    private final Map<CommandSender, UserStockHolder> confirmationMap = new HashMap<>();

    public ResetAllCommand() {
        super("resetall", "box.admin.command.resetall");
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.RESET_ALL_HELP;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(getHelp());
            return;
        }

        if (CONFIRM.equalsIgnoreCase(args[1]) && confirmationMap.containsKey(sender)) {
            var target = confirmationMap.remove(sender);

            target.reset();

            BoxProvider.get().getEventBus().callEvent(new UserDataResetEvent(target.getUser()));

            sender.sendMessage(BoxAdminMessage.RESET_ALL_SUCCESS_SENDER.apply(target));

            var targetPlayer = Bukkit.getPlayer(target.getUUID());

            if (targetPlayer != null && !sender.getName().equals(targetPlayer.getName())) {
                targetPlayer.sendMessage(BoxAdminMessage.RESET_ALL_SUCCESS_TARGET.apply(sender));
            }

            return;
        }

        if (CANCEL.equalsIgnoreCase(args[1]) && confirmationMap.remove(sender) != null) {
            sender.sendMessage(BoxAdminMessage.RESET_ALL_CANCEL);
            return;
        }

        UserStockHolderOperator.create(args[1])
                .supportOffline(true)
                .stockHolderOperator(targetStockHolder -> {
                    confirmationMap.put(sender, targetStockHolder);
                    sender.sendMessage(BoxAdminMessage.RESET_ALL_CONFIRMATION.apply(targetStockHolder));
                })
                .onNotFound(name -> sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(name)))
                .run();
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
}