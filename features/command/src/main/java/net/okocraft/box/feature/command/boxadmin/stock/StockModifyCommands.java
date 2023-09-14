package net.okocraft.box.feature.command.boxadmin.stock;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class StockModifyCommands {

    static @NotNull Command give() {
        return new ModifyCommand("give", Set.of("g"), false) {

            @Override
            int modifyStock(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
                return stockHolder.increase(item, amount, new CommandCauses.AdminGive(sender));
            }

            @Override
            void sendMessage(@NotNull CommandSender sender, @NotNull BoxUser target, BoxItem item, int amount, int current) {
                sender.sendMessage(BoxAdminMessage.STOCK_GIVE_SUCCESS_SENDER.apply(target, item, amount, current));

                var targetPlayer = Bukkit.getPlayer(target.getUUID());

                if (targetPlayer != null && sender != targetPlayer) {
                    targetPlayer.sendMessage(
                            BoxAdminMessage.STOCK_GIVE_SUCCESS_TARGET.apply(sender.getName(), item, amount, current)
                    );
                }
            }

            @Override
            public @NotNull Component getHelp() {
                return BoxAdminMessage.STOCK_GIVE_HELP;
            }
        };
    }

    static @NotNull Command set() {
        return new ModifyCommand("set", Set.of("s"), true) {

            @Override
            int modifyStock(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
                stockHolder.setAmount(item, amount, new CommandCauses.AdminSet(sender));
                return amount;
            }

            @Override
            void sendMessage(@NotNull CommandSender sender, @NotNull BoxUser target, BoxItem item, int amount, int current) {
                sender.sendMessage(BoxAdminMessage.STOCK_SET_SUCCESS_SENDER.apply(target, item, current));

                var targetPlayer = Bukkit.getPlayer(target.getUUID());

                if (targetPlayer != null && sender != targetPlayer) {
                    targetPlayer.sendMessage(
                            BoxAdminMessage.STOCK_SET_SUCCESS_TARGET.apply(sender.getName(), item, current)
                    );
                }
            }

            @Override
            public @NotNull Component getHelp() {
                return BoxAdminMessage.STOCK_SET_HELP;
            }
        };
    }

    static @NotNull Command take() {
        return new ModifyCommand("take", Set.of("t"), false) {

            @Override
            int modifyStock(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
                return stockHolder.decrease(item, amount, new CommandCauses.AdminTake(sender));
            }

            @Override
            void sendMessage(@NotNull CommandSender sender, @NotNull BoxUser target, BoxItem item, int amount, int current) {
                sender.sendMessage(BoxAdminMessage.STOCK_TAKE_SUCCESS_SENDER.apply(target, item, amount, current));

                var targetPlayer = Bukkit.getPlayer(target.getUUID());

                if (targetPlayer != null && sender != targetPlayer) {
                    targetPlayer.sendMessage(
                            BoxAdminMessage.STOCK_TAKE_SUCCESS_TARGET.apply(sender.getName(), item, amount, current)
                    );
                }
            }

            @Override
            public @NotNull Component getHelp() {
                return BoxAdminMessage.STOCK_TAKE_HELP;
            }
        };
    }

    private static abstract class ModifyCommand extends AbstractCommand {

        private final boolean allowZero;

        private ModifyCommand(@NotNull String name, @NotNull Set<String> aliases, boolean allowZero) {
            super(name, "box.admin.command.stock." + name, aliases);
            this.allowZero = allowZero;
        }

        @Override
        public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
            if (args.length < 5) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
                sender.sendMessage(getHelp());
                return;
            }

            var item = BoxProvider.get().getItemManager().getBoxItem(args[3]);

            if (item.isEmpty()) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[3]));
                return;
            }

            int amount;

            try {
                amount = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[4]));
                return;
            }

            if (amount <= 0) {
                if (amount != 0 || !allowZero) {
                    sender.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[4]));
                    return;
                }
            }

            var target = UserSearcher.search(args[2]);

            if (target != null) {
                var stockHolder = BoxProvider.get().getStockManager().getPersonalStockHolderLoader(target);
                int current = modifyStock(sender, stockHolder, item.get(), amount);
                sendMessage(sender, target, item.get(), amount, current);
            } else {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[2]));
            }
        }

        abstract int modifyStock(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount);

        abstract void sendMessage(@NotNull CommandSender sender, @NotNull BoxUser user, BoxItem item, int amount, int current);

        @Override
        public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
            if (args.length == 3) {
                return TabCompleter.players(args[2]);
            }

            if (args.length == 4) {
                return TabCompleter.itemNames(args[3]);
            }

            return Collections.emptyList();
        }
    }

    private StockModifyCommands() {
        throw new UnsupportedOperationException();
    }
}
