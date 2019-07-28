package net.okocraft.box.command.boxadmin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.box.util.OtherUtil;

public class Give extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "give";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/boxadmin give <player> <ITEM> [amount]";
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }
        String player = args[1].toLowerCase();
        String item   = args[2].toUpperCase();
        long amount = args.length < 4 ? 1 : OtherUtil.parseLongOrDefault(args[3], 1);

        Long currentAmount;
        
        try {
            currentAmount = Long.parseLong(DATABASE.get(item, player));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            sender.sendMessage(MESSAGE_CONFIG.getInvalidNumberFormat());
            return false;
        }

        String value = String.valueOf(currentAmount + amount);
        DATABASE.set(item, player, value);

        sender.sendMessage(
                MESSAGE_CONFIG.getSuccessGiveAdmin()
                        .replaceAll("%player%", player)
                        .replaceAll("%item%", item)
                        .replaceAll("%amount%", String.valueOf(amount))
                        .replaceAll("%newamount%", value)
        );

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> players = new ArrayList<>(DATABASE.getPlayersMap().values());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, result);
        }

        if (!players.contains(args[1].toLowerCase())) {
            return List.of();
        }

        List<String> items = CONFIG.getAllItems();

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        if (!items.contains(args[2].toUpperCase())) {
            return List.of();
        }

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], List.of("1", "10", "100", "1000", "10000"), result);
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
        return MESSAGE_CONFIG.getGiveAdminDesc();
    }

    @Override
    protected boolean validate(CommandSender sender, String[] args) {
        if (!super.validate(sender, args)) {
            return false;
        }

        if (!DATABASE.existPlayer(args[1].toLowerCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return false;
        }

        if (!CONFIG.getAllItems().contains(args[2].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return false;
        }

        return true;
    }
}