package net.okocraft.box.command.box;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.util.OtherUtil;
import net.okocraft.box.util.PlayerUtil;

public class AutoStoreList extends BaseSubCommand {

    private static final String COMMAND_NAME = "autostorelist";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box autostorelist <page>";

    public AutoStoreList() {
        super();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        String player = ((Player) sender).getUniqueId().toString();

        int index = args.length >= 2 ? OtherUtil.parseIntOrDefault(args[1], 1) : 1;

        List<String> allItems = CONFIG.getAllItems();
        
        int maxLine = allItems.size();
        int currentLine = (maxLine < index * 8) ? maxLine : index * 8;

        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreListHeader()
                        .replaceAll("%player%", sender.getName().toLowerCase())
                        .replaceAll("%page%", String.valueOf(index))
                        .replaceAll("%currentLine%", String.valueOf(currentLine))
                        .replaceAll("%maxLine%", String.valueOf(maxLine))
        );

        List<String> columnList = CONFIG.getAllItems().stream()
                .skip(8 * (index - 1)).limit(8)
                .map(name -> "autostore_" + name)
                .collect(Collectors.toList());

        DATABASE.getMultiValue(columnList, player).forEach((columnName, value) ->
                sender.sendMessage(
                        MESSAGE_CONFIG.getAutoStoreListFormat()
                                .replaceAll("%item%", columnName.substring(10))
                                .replaceAll("%isEnabled%", value)
                                .replaceAll("%currentLine%", String.valueOf(currentLine))
                                .replaceAll("%maxLine%", String.valueOf(maxLine))
                )
        );

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        int items = CONFIG.getAllItems().size();
        int maxPage = items % 8 == 0 ? items / 8 : items / 8 + 1;
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
        return MESSAGE_CONFIG.getAutoStoreListDesc();
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

        // If the player isn't registered
        if (PlayerUtil.notExistPlayer(sender)) {
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return false;
        }

        return true;
    }
}