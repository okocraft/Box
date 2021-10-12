package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import net.okocraft.box.feature.command.util.TabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StockModifyCommands {

    public static @NotNull Command give() {
        return new ModifyCommand("give", Set.of("g")) {

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
        return new ModifyCommand("set", Set.of("s")) {

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
        return new ModifyCommand("take", Set.of("t")) {

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

        private ModifyCommand(@NotNull String name, @NotNull Set<String> aliases) {
            super(name, "box.admin.command." + name, aliases);
        }

        @Override
        public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
            if (args.length < 4) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
                sender.sendMessage(getHelp());
                return;
            }

            try {
                var uuid = UUID.fromString(args[1]);
                processUUID(sender, uuid, args);
            } catch (IllegalArgumentException ignored) {
                processPlayerName(sender, args);
            }
        }

        private void processUUID(@NotNull CommandSender sender, @NotNull UUID uuid,
                                 @NotNull String[] args) {
            var player = Bukkit.getPlayer(uuid);

            if (player != null) {
                var boxPlayer = BoxProvider.get().getBoxPlayerMap().get(player);
                processCommand(sender, boxPlayer.getUserStockHolder(), player, args);
                return;
            }

            var boxUser = BoxProvider.get().getUserManager().loadUser(uuid).join();
            var userStockHolder = BoxProvider.get().getStockManager().loadUserStock(boxUser).join();
            processCommand(sender, userStockHolder, null, args);
        }

        private void processPlayerName(@NotNull CommandSender sender, @NotNull String[] args) {
            var player = Bukkit.getPlayer(args[1]);

            if (player != null) {
                processPlayer(sender, player, args);
                return;
            }

            var boxUser = BoxProvider.get().getUserManager().search(args[1]).join();

            if (boxUser.isPresent()) {
                processBoxUser(sender, boxUser.get(), args);
            } else {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[1]));
            }
        }

        private void processPlayer(@NotNull CommandSender sender, @NotNull Player target,
                                   @NotNull String[] args) {
            var boxPlayer = BoxProvider.get().getBoxPlayerMap().get(target);
            processCommand(sender, boxPlayer.getUserStockHolder(), target, args);
        }

        private void processBoxUser(@NotNull CommandSender sender, @NotNull BoxUser target,
                                    @NotNull String[] args) {
            var userStockHolder = BoxProvider.get().getStockManager().loadUserStock(target).join();
            processCommand(sender, userStockHolder, null, args);
        }

        private void processCommand(@NotNull CommandSender sender, @NotNull UserStockHolder target,
                                    @Nullable Player targetPlayer, @NotNull String[] args) {
            var item = BoxProvider.get().getItemManager().getBoxItem(args[2]);

            if (item.isEmpty()) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[2]));
                return;
            }

            int amount;

            try {
                amount = Math.max(Integer.parseInt(args[3]), 1);
            } catch (NumberFormatException e) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[3]));
                return;
            }

            int current = modifyStock(target, item.get(), amount);

            if (targetPlayer == null) {
                BoxProvider.get().getStockManager().saveUserStock(target).join();
            }

            sendMessage(sender, targetPlayer, target.getName(), item.get(), amount, current);
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
