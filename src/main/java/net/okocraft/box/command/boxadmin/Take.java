package net.okocraft.box.command.boxadmin;

import net.okocraft.box.util.OtherUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Take extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "take";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/boxadmin take <player> <ITEM> [amount]";

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (validate(sender, args)) {
            return false;
        }
        String player = args[1].toLowerCase();
        String item   = args[2].toUpperCase();
        long amount = args.length < 4 ? 1 : OtherUtil.parseLongOrDefault(args[3], 1);

        long currentAmount;
        
        try {
            currentAmount = Long.parseLong(DATABASE.get(item, player));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            sender.sendMessage(MESSAGE_CONFIG.getInvalidNumberFormat());
            return false;
        }

        long rawValue = currentAmount - amount;
        String value = String.valueOf(rawValue < 1 ? 1 : rawValue);
        DATABASE.set(item, player, value);

        sender.sendMessage(
                MESSAGE_CONFIG.getSuccessTake()
                        .replaceAll("%player%", player)
                        .replaceAll("%item%", item)
                        .replaceAll("%amount%", String.valueOf(amount))
                        .replaceAll("%newamount%", value)
        );

        return true;
    }

    @Override
    public List<String> onTabComplete(String[] args) {
        List<String> result = new ArrayList<>();

        List<String> players = new ArrayList<>(DATABASE.getPlayersMap().values());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, result);
        }

        String player = args[1].toLowerCase();

        if (!players.contains(player)) {
            return List.of();
        }

        List<String> items = CONFIG.getAllItems();

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
            return List.of("0");
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
        return MESSAGE_CONFIG.getTakeDesc();
    }

    @Override
    boolean validate(CommandSender sender, String[] args) {
        if (super.validate(sender, args)) {
            return true;
        }

        if (!DATABASE.existPlayer(args[1].toLowerCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return true;
        }

        if (!CONFIG.getAllItems().contains(args[2].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return true;
        }

        return false;
    }
}