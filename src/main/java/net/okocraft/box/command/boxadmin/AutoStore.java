package net.okocraft.box.command.boxadmin;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AutoStore extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "autostore";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/boxadmin autostore <player> < <ITEM> [true|false] | ALL <true|false> >";

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (validate(sender, args)) {
            return false;
        }

        String player = args[1].toUpperCase();
        String itemName = args[2].toUpperCase();
        String switchTo = args.length < 4 ? null : args[3].toLowerCase();

        // autostore all <true|false>
        if (itemName.equalsIgnoreCase("all")) {
            // validateにより、nullのままこのifに進むことはない。
            assert switchTo != null;
            return autoStoreAll(sender, player, switchTo);
        }

        // autostore Item [true|false]
        return autoStore(sender, player, itemName, switchTo);
    }

    /**
     * アイテム１つのautoStore設定を変更する。
     * 
     * @param sender コマンドの実行者
     * @return 変更に成功なら {@code true}
     */
    private boolean autoStore(CommandSender sender, String player, String itemName, @Nullable String switchTo) {
        if (switchTo == null) {
            boolean now = DATABASE.get("autostore_" + itemName, player).equalsIgnoreCase("true");
            switchTo = now ? "false" : "true";
        }

        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreSettingChanged()
                        .replaceAll("%item%", itemName).replaceAll("%isEnabled%", switchTo)
        );

        DATABASE.set("autostore_" + itemName, player, switchTo);

        return true;
    }

    /**
     * アイテムすべてのautoStore設定を変更する。
     * 
     * @param sender コマンドを実行した人
     * @return 変更に成功なら {@code true}
     */
    private boolean autoStoreAll(CommandSender sender, String player, String switchTo) {
        List<String> allItems = CONFIG.getAllItems();

        Map<String, String> newValues = allItems.stream()
                .collect(Collectors.toMap(itemNameTemp ->
                                "autostore_" + itemNameTemp,
                        itemNameTemp -> switchTo.toLowerCase(),
                        (e1, e2) -> e1, HashMap::new
                ));

        DATABASE.setMultiValue(newValues, player);

        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreSettingChangedAll()
                        .replaceAll("%isEnabled%", switchTo.toLowerCase())
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

        List<String> items = new ArrayList<>(CONFIG.getAllItems());
        items.add("ALL");

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        if (!items.contains(args[2].toUpperCase())) {
            return List.of();
        }

        if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], List.of("true", "false"), result);
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
        return MESSAGE_CONFIG.getAutoStoreDesc();
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
        
        if (!args[2].equalsIgnoreCase("ALL") && !CONFIG.getAllItems().contains(args[2].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return true;
        }

        if (args.length < 4 && args[2].equalsIgnoreCase("ALL")) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughArguments());
            return true;
        }

        if (args.length >= 4) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return true;
        }

        return false;
    }
}