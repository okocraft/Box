package net.okocraft.box.command.box;

import net.okocraft.box.util.OtherUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Help extends BaseSubCommand {

    private static final String COMMAND_NAME = "help";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/box help [page]";

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (validate(sender, args)) {
            return false;
        }

        BoxCommand commands = INSTANCE.getCommand();

        Map<String, String> commandDescriptionMap = new LinkedHashMap<>() {
            private static final long serialVersionUID = 1L;

            {
                put(commands.getUsage(), commands.getDescription());
                commands.getSubCommandMap().values().forEach(subCommand -> put(subCommand.getUsage(), subCommand.getDescription()));
            }
        };

        int mapSize = commands.getSubCommandMapSize();
        int page = args.length > 1 ? OtherUtil.parseIntOrDefault(args[1], 1) : 1;
        int maxPage = mapSize % 8 == 0 ? mapSize / 8 : mapSize / 8 + 1;
        if (page > maxPage) {
            page = maxPage;
        }

        sender.sendMessage(MESSAGE_CONFIG.getCommandHelpHeader());
        commandDescriptionMap.entrySet().stream().skip(8 * (page - 1)).limit(8).forEach(entry -> sender.sendMessage("§b" + entry.getKey() + "§7 - " + entry.getValue()));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        int mapSize = INSTANCE.getCommand().getSubCommandMapSize();
        int maxPage = mapSize % 8 == 0 ? mapSize / 8 : mapSize / 8 + 1;
        List<String> pages  = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf).collect(Collectors.toList());
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
        return MESSAGE_CONFIG.getHelpDesc();
    }
}