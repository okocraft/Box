package net.okocraft.box.command.boxadmin;

import net.okocraft.box.util.OtherUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

class Set extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "set";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/boxadmin set <player> <ITEM> <amount>";

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (validate(sender, args)) {
            return false;
        }
        String player = args[1].toLowerCase();
        String item   = args[2].toUpperCase();

        long amount = args.length < 4 ? 0 : OtherUtil.parseLongOrDefault(args[3], 0);

        DATABASE.set(item, player, String.valueOf(amount));

        sender.sendMessage(
                MESSAGE_CONFIG.getSuccessSet()
                        .replaceAll("%player%", player)
                        .replaceAll("%item%", item)
                        .replaceAll("%amount%", String.valueOf(amount))
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
        return MESSAGE_CONFIG.getSetDesc();
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