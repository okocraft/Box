package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import net.okocraft.box.feature.command.util.TabCompleter;
import net.okocraft.box.feature.command.util.UserStockHolderOperator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResetCommand extends AbstractCommand {

    private static final String CONFIRM = "confirm";
    private static final String CANCEL = "cancel";

    private final Map<CommandSender, UserStockHolder> confirmationMap = new HashMap<>();

    public ResetCommand() {
        super("reset", "box.admin.command.reset");
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.RESET_HELP;
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

            for (var stockedItem : target.getStockedItems()) {
                target.setAmount(stockedItem, 0);
            }

            sender.sendMessage(BoxAdminMessage.RESET_SUCCESS_SENDER.apply(target));

            var targetPlayer = Bukkit.getPlayer(target.getUUID());

            if (targetPlayer != null && !sender.getName().equals(targetPlayer.getName())) {
                targetPlayer.sendMessage(BoxAdminMessage.RESET_SUCCESS_TARGET.apply(sender));
            } else {
                BoxProvider.get().getStockManager().saveUserStock(target).join();
            }

            return;
        }

        if (CANCEL.equalsIgnoreCase(args[1]) && confirmationMap.remove(sender) != null) {
            sender.sendMessage(BoxAdminMessage.RESET_CANCEL);
            return;
        }

        UserStockHolderOperator.create(args[1])
                .supportOffline(true)
                .stockHolderOperator(targetStockHolder -> {
                    confirmationMap.put(sender, targetStockHolder);
                    sender.sendMessage(BoxAdminMessage.RESET_CONFIRMATION.apply(targetStockHolder));
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
