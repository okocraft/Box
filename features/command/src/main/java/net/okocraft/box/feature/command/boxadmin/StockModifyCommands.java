package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserStockHolderOperator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StockModifyCommands {

    public static @NotNull Command give() {
        return new ModifyCommand("give", Set.of("g"), false) {

            @Override
            int modifyStock(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
                return stockHolder.increase(item, amount);
            }

            @Override
            void sendMessage(@NotNull CommandSender sender, @Nullable Player targetPlayer,
                             @NotNull String targetName, BoxItem item, int amount, int current) {
                sender.sendMessage(BoxAdminMessage.GIVE_SUCCESS_SENDER.apply(targetName, item, amount, current));

                if (targetPlayer != null && !sender.getName().equals(targetName)) {
                    targetPlayer.sendMessage(
                            BoxAdminMessage.GIVE_SUCCESS_TARGET.apply(sender.getName(), item, amount, current)
                    );
                }
            }

            @Override
            public @NotNull Component getHelp() {
                return BoxAdminMessage.GIVE_HELP;
            }
        };
    }

    public static @NotNull Command set() {
        return new ModifyCommand("set", Set.of("s"), true) {

            @Override
            int modifyStock(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
                stockHolder.setAmount(item, amount);
                return amount;
            }

            @Override
            void sendMessage(@NotNull CommandSender sender, @Nullable Player targetPlayer,
                             @NotNull String targetName, BoxItem item, int amount, int current) {
                sender.sendMessage(BoxAdminMessage.SET_SUCCESS_SENDER.apply(targetName, item, current));

                if (targetPlayer != null && !sender.getName().equals(targetName)) {
                    targetPlayer.sendMessage(
                            BoxAdminMessage.SET_SUCCESS_TARGET.apply(sender.getName(), item, current)
                    );
                }
            }

            @Override
            public @NotNull Component getHelp() {
                return BoxAdminMessage.SET_HELP;
            }
        };
    }

    public static @NotNull Command take() {
        return new ModifyCommand("take", Set.of("t"), false) {

            @Override
            int modifyStock(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
                return stockHolder.decrease(item, amount);
            }

            @Override
            void sendMessage(@NotNull CommandSender sender, @Nullable Player targetPlayer,
                             @NotNull String targetName, BoxItem item, int amount, int current) {
                sender.sendMessage(BoxAdminMessage.TAKE_SUCCESS_SENDER.apply(targetName, item, amount, current));

                if (targetPlayer != null && !sender.getName().equals(targetName)) {
                    targetPlayer.sendMessage(
                            BoxAdminMessage.TAKE_SUCCESS_TARGET.apply(sender.getName(), item, amount, current)
                    );
                }
            }

            @Override
            public @NotNull Component getHelp() {
                return BoxAdminMessage.TAKE_HELP;
            }
        };
    }

    private static abstract class ModifyCommand extends AbstractCommand {

        private final boolean allowZero;

        private ModifyCommand(@NotNull String name, @NotNull Set<String> aliases, boolean allowZero) {
            super(name, "box.admin.command." + name, aliases);
            this.allowZero = allowZero;
        }

        @Override
        public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
            if (args.length < 4) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
                sender.sendMessage(getHelp());
                return;
            }

            var item = BoxProvider.get().getItemManager().getBoxItem(args[2]);

            if (item.isEmpty()) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[2]));
                return;
            }

            int amount;

            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[3]));
                return;
            }

            if (!allowZero && amount == 0) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[3]));
                return;
            }

            UserStockHolderOperator.create(args[1])
                    .supportOffline(true)
                    .stockHolderOperator(target -> {
                        int current = modifyStock(target, item.get(), amount);
                        sendMessage(sender, Bukkit.getPlayer(target.getUUID()), target.getName(), item.get(), amount, current);
                    })
                    .onNotFound(name -> sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(name)))
                    .run();
        }

        abstract int modifyStock(@NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount);

        abstract void sendMessage(@NotNull CommandSender sender, @Nullable Player targetPlayer,
                                  @NotNull String targetName, BoxItem item, int amount, int current);

        @Override
        public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
            if (args.length == 2) {
                return TabCompleter.players(args[1]);
            }

            if (args.length == 3) {
                return TabCompleter.itemNames(args[2]);
            }

            return Collections.emptyList();
        }
    }

    private StockModifyCommands() {
        throw new UnsupportedOperationException();
    }
}
