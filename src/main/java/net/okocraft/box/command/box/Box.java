package net.okocraft.box.command.box;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import lombok.Getter;
import net.okocraft.box.command.BaseBoxCommand;
import net.okocraft.box.gui.CategorySelectorGUI;

public class Box extends BaseBoxCommand implements CommandExecutor, TabCompleter {

    private static final String COMMAND_NAME = "box";
    private static final String USAGE = "/box [args...]";
    
    @Getter
    private Map<String, BaseSubCommand> subCommandMap;
    @Getter
    private final int subCommandMapSize;

    public Box() {
        subCommandMap = new HashMap<String, BaseSubCommand>(){
            private static final long serialVersionUID = 1L;
            {
                Version version = new Version();
                put(version.getCommandName(), version);
    
                Help help = new Help();
                put(help.getCommandName(), help);
    
                AutoStoreList autoStoreList = new AutoStoreList();
                put(autoStoreList.getCommandName(), autoStoreList);
    
                AutoStore autoStore = new AutoStore();
                put(autoStore.getCommandName(), autoStore);
    
                Give give = new Give();
                put(give.getCommandName(), give);
    
                Sell sell = new Sell();
                put(sell.getCommandName(), sell);
    
                SellPrice sellPrice = new SellPrice();
                put(sellPrice.getCommandName(), sellPrice);
    
                SellPriceList sellPriceList = new SellPriceList();
                put(sellPriceList.getCommandName(), sellPriceList);
            }
        };

        subCommandMapSize = subCommandMap.size();

        INSTANCE.getCommand(getCommandName()).setExecutor(this);
        INSTANCE.getCommand(getCommandName()).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return runCommand(sender, args);
        }
        BaseSubCommand subCommand = subCommandMap.get(args[0].toLowerCase());
        if (subCommand == null) {
            sender.sendMessage(MESSAGE_CONFIG.getNoParamExist());
            return false;
        }
        return subCommand.runCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return null;
        }

        List<String> permedSubCommands = subCommandMap.entrySet().stream()
                .filter(entry -> sender.hasPermission(entry.getValue().getPermissionNode()))
                .map(Map.Entry::getKey).collect(Collectors.toList());

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(
                    args[0],
                    permedSubCommands,
                    new ArrayList<>()
            );
        }
        
        BaseSubCommand subCommand = subCommandMap.get(args[0].toLowerCase());
        if (subCommand == null || !permedSubCommands.contains(subCommand.getCommandName())) {
            return List.of();
        }
        return subCommand.runTabComplete(sender, args);
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MESSAGE_CONFIG.getErrorOccurredOnGUI();
            return false;
        }
        ((Player) sender).openInventory(CategorySelectorGUI.GUI);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("Non subcommand could not be completed with this method.");
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return MESSAGE_CONFIG.getBoxDesc();
    }

    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public String getPermissionNode() {
        return getCommandName();
    }

    @Override
    public int getLeastArgLength() {
        return 0;
    }
}