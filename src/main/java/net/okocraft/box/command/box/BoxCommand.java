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
import net.okocraft.box.Box;
import net.okocraft.box.gui.CategorySelectorGUI;
import net.okocraft.box.util.MessageConfig;

public class BoxCommand implements CommandExecutor, TabCompleter {

    private static final Box INSTANCE = Box.getInstance();
    private static final MessageConfig MESSAGE_CONFIG = INSTANCE.getMessageConfig();
    private static final String USAGE = "/box [args...]";
    
    @Getter
    private final Map<String, BaseSubCommand> subCommandMap;
    @Getter
    private final int subCommandMapSize;

    public BoxCommand() {
        subCommandMap = new HashMap<>() {
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

        INSTANCE.getCommand("box").setExecutor(this);
        INSTANCE.getCommand("box").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return runCommand(sender);
        }
        BaseSubCommand subCommand = subCommandMap.get(args[0].toLowerCase());
        if (subCommand == null) {
            sender.sendMessage(MESSAGE_CONFIG.getNoParamExist());
            return false;
        }
        return subCommand.onCommand(sender, args);
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
        return subCommand.onTabComplete(sender, args);
    }

    /**
     * コマンドの名前を取得する。
     * 
     * @return
     */
    public String getCommandName() {
        return "box";
    }

    /**
     * コマンドの説明を取得する。例: "アイテムの自動収納の設定をリストにして表示する。"
     * 
     * @return コマンドの説明
     */
    public String getDescription() {
        return MESSAGE_CONFIG.getBoxDesc();
    }

    /**
     * コマンドの引数の内容を取得する。例: "/box autostoreList [page]"
     * 
     * @return 引数の内容
     */
    public String getUsage() {
        return USAGE;
    }

    private boolean runCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MESSAGE_CONFIG.getErrorOccurredOnGUI();
            return false;
        }
        ((Player) sender).openInventory(CategorySelectorGUI.GUI);
        return true;
    }
}