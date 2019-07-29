package net.okocraft.box.command.box;

import net.okocraft.box.util.PlayerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SellPrice extends BaseSubCommand {

    private static final String COMMAND_NAME = "sellprice";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box sellprice <ITEM>";

    SellPrice() {
        super();
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (validate(sender, args)) {
            return false;
        }

        String item = args[1].toUpperCase();
        double price = Optional.ofNullable(CONFIG.getSellPrice().get(item)).orElse(0);
        sender.sendMessage(
            MESSAGE_CONFIG.getSellPriceFormat()
                    .replaceAll("%item%", item)
                    .replaceAll("%price%", String.valueOf(price))
        );

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        
        List<String> items = new ArrayList<>(CONFIG.getSellPrice().keySet());
        
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], items, result);
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
        return MESSAGE_CONFIG.getSellPriceDesc();
    }


    @Override
    boolean validate(CommandSender sender, String[] args) {
        if (super.validate(sender, args)) {
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(MESSAGE_CONFIG.getPlayerOnly());
            return true;
        }

        if (INSTANCE.getEconomy() == null) {
            sender.sendMessage(MESSAGE_CONFIG.getEconomyIsNull());
            return true;
        }
        
        if (!CONFIG.getAllItems().contains(args[1].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return true;
        }

        if (PlayerUtil.notExistPlayer(sender)) {
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return true;
        }

        return false;
    }
}