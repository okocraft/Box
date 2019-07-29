package net.okocraft.box.command.box;

import net.okocraft.box.util.OtherUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class SellPriceList extends BaseSubCommand {

    private static final String COMMAND_NAME = "sellpricelist";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/box sellpricelist [page]";

    SellPriceList() {
        super();
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (validate(sender, args)) {
            return false;
        }
        int page = args.length >= 2 ? OtherUtil.parseIntOrDefault(args[1], 1) : 1;

        int maxLine = CONFIG.getAllItems().size();
        int currentLine = Math.min(maxLine, page * 8);

        sender.sendMessage(
                MESSAGE_CONFIG.getSellPriceListHeader()
                        .replaceAll("%currentLine%", String.valueOf(currentLine))
                        .replaceAll("%maxLine%", String.valueOf(maxLine))
        );

        CONFIG.getSellPrice().entrySet().stream().skip(8 * (page - 1)).limit(8)
                .forEach(mapEntry -> sender.sendMessage(MESSAGE_CONFIG.getSellPriceListFormat()
                        .replaceAll("%item%", mapEntry.getKey())
                        .replaceAll("%price%", String.valueOf(mapEntry.getValue()))
                ));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        int items = CONFIG.getSellPrice().size();
        int maxPage = items % 8 == 0 ? items : items + 1;

        List<String> pages = IntStream.rangeClosed(0, maxPage).boxed().map(String::valueOf).collect(Collectors.toList());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], pages, result);
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
        return MESSAGE_CONFIG.getSellPriceListDesc();
    }


    @Override
    boolean validate(CommandSender sender, String[] args) {
        if (super.validate(sender, args)) {
            return true;
        }

        if (INSTANCE.getEconomy() == null) {
            sender.sendMessage(MESSAGE_CONFIG.getEconomyIsNull());
            return true;
        }

        return false;
    }
}