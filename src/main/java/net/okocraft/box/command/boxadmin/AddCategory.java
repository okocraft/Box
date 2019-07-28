package net.okocraft.box.command.boxadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.listeners.GenerateItemConfig;

public class AddCategory extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "addcategory";
    private static final int LEAST_ARG_LENGTH = 5;
    private static final String USAGE = "/boxadmin addcategory <category> <id> <displayName> <iconMaterial>";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        sender.sendMessage("[Box] チェストを選択してください");
        new GenerateItemConfig((Player) sender, args[1], args[2], args[3], args[4]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        switch (args.length) {
        case 2:
            return StringUtil.copyPartialMatches(args[1], List.of("<category>"), result);
        case 3:
            return StringUtil.copyPartialMatches(args[2], List.of("<id>"), result);
        case 4:
            return StringUtil.copyPartialMatches(args[3], List.of("<display_name>"), result);
        case 5:
            List<String> items = Arrays.asList(Material.values()).stream()
                    .map(Material::name).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[4], items, result);
        default:
            return result;
        }
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
        return MESSAGE_CONFIG.getAddCategoryDesc();
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

        return true;
    }
}