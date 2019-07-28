package net.okocraft.box.command.box;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.util.OtherUtil;
import net.okocraft.box.util.PlayerUtil;

public class Give extends BaseSubCommand {

    private static final String COMMAND_NAME = "give";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/box give <player> <ITEM> [amount]";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        String senderName = sender.getName().toLowerCase();
        String player = args[1].toLowerCase();

        String itemName = args[2].toUpperCase();


        Long amount = args.length == 3 ? 1L : OtherUtil.parseLongOrDefault(args[3], 1L);

        if (amount < 1) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return false;
        }

        long senderAmount = OtherUtil.parseLongOrDefault(DATABASE.get(itemName, senderName), Long.MIN_VALUE);
        long otherAmount  = OtherUtil.parseLongOrDefault(DATABASE.get(itemName, player), Long.MIN_VALUE);

        if (senderAmount == Long.MIN_VALUE || otherAmount == Long.MIN_VALUE) {
            sender.sendMessage(
                    MESSAGE_CONFIG.getDatabaseInvalidValue()
            );

            return false;
        }

        if (senderAmount - amount < 0) {
            sender.sendMessage(
                    MESSAGE_CONFIG.getNotEnoughStoredItem()
            );

            return false;
        }

        DATABASE.set(itemName, senderName, String.valueOf(senderAmount - amount));
        DATABASE.set(itemName, player, String.valueOf(otherAmount + amount));

        sender.sendMessage(
                MESSAGE_CONFIG.getSuccessGive()
                        .replaceAll("%player%", player)
                        .replaceAll("%item%", itemName)
                        .replaceAll("%amount%", amount.toString())
                        .replaceAll("%newamount%", String.valueOf(senderAmount - amount))
        );

        OfflinePlayer offlinePlayer = PlayerUtil.getOfflinePlayer(player);

        if (offlinePlayer.isOnline()) {
            Optional.ofNullable(offlinePlayer.getPlayer()).ifPresent( _player ->
                    _player.sendMessage(
                            MESSAGE_CONFIG.getSuccessReceive()
                                    .replaceAll("%player%", senderName)
                                    .replaceAll("%item%", itemName)
                                    .replaceAll("%amount%", amount.toString())
                                    .replaceAll("%newamount%", String.valueOf(otherAmount + amount))
                    )
            );
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        List<String> players = new ArrayList<>(DATABASE.getPlayersMap().values());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, result);
        }

        String player = args[1].toLowerCase();

        if (!players.contains(player)) {
            return List.of();
        }

        List<String> items = DATABASE.getMultiValue(CONFIG.getAllItems(), player).entrySet()
                .stream().filter(entry -> !entry.getValue().equals("0")).map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        String item = args[2].toUpperCase();

        if (!items.contains(item)) {
            return List.of();
        }
        
        String rawStock = DATABASE.get(item, player);
        long stock;
        try {
            stock = Long.parseLong(rawStock);
        } catch (NumberFormatException e) {
            return List.of();
        }

        if (stock < 1) {
            return List.of();
        }

        List<String> amountList = IntStream.iterate(1, n -> n * 10).limit(10).filter(n -> n < stock)
                .boxed().map(String::valueOf).collect(Collectors.toList());
        amountList.add(String.valueOf(stock));

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], amountList, result);
        }

        return result;
    }

    @Override
    String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @Override
    String getUsage() {
        return USAGE;
    }

    @Override
    String getDescription() {
        return MESSAGE_CONFIG.getGiveDesc();
    }


    @Override
    protected boolean validate(CommandSender sender, String[] args) {
        if (!super.validate(sender, args)) {
            return false;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(MESSAGE_CONFIG.getPlayerOnly());
            return false;
        }

        if (sender.getName().equalsIgnoreCase(args[1])) {
            sender.sendMessage(MESSAGE_CONFIG.getCannotGiveYourself());
            return false;
        }

        // アイテムが登録されていない
        if (!CONFIG.getAllItems().contains(args[2].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return false;
        }

        Map<String, String> players = DATABASE.getPlayersMap();
        // プレイヤーがデータベースに登録されていない
        if (
            !players.containsKey(args[1].toLowerCase()) ||
            !players.containsValue(args[1].toLowerCase()) ||
            !players.containsKey(sender.getName().toLowerCase()) ||
            !players.containsValue(sender.getName().toLowerCase())
        ){
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return false;
        }

        return true;
    }
}