/*
 * Box
 * Copyright (C) 2019 OKOCRAFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.okocraft.box.command.box;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.util.PlayerUtil;

class AutoStore extends BaseSubCommand {

    private static final String COMMAND_NAME = "autostore";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box autostore < <ITEM> [true|false] | ALL <true|false> >";

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        String item = args[1].toUpperCase();
        // switchTo が null の場合の処理はどうするのか
        String switchTo = args.length > 2 ? args[2] : null;

        // autostore all <true|false>
        if (item.equals("ALL")) {
            return autoStoreAll(sender, switchTo);
        }

        // autostore Item [true|false]
        return autoStore(sender, item, switchTo);
    }

    /**
     * アイテム１つのautoStore設定を変更する。
     * 
     * @param sender
     * @param item
     * @param switchTo
     * 
     * @return 実行に成功したら {@code true}
     */
    private boolean autoStore(CommandSender sender, String item, @Nullable String switchTo) {
        String player = ((Player) sender).getUniqueId().toString();

        if (switchTo == null) {
            boolean now = DATABASE.get("autostore_" + item, player).equalsIgnoreCase("true");
            switchTo = now ? "false" : "true";
        }

        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreSettingChanged()
                        .replaceAll("%item%", item).replaceAll("%isEnabled%", switchTo)
        );

        DATABASE.set("autostore_" + item, player, switchTo);

        return true;
    }

    /**
     * アイテムすべてのautoStore設定を変更する。
     * 
     * @param sender
     * @param switchTo
     * @return  実行に成功したら {@code true}
     */
    private boolean autoStoreAll(CommandSender sender, String switchTo) {
        String player = ((Player) sender).getUniqueId().toString();
        List<String> allItems = CONFIG.getAllItems();

        // If switchTo is neither true nor false
        if (!switchTo.equalsIgnoreCase("true") && !switchTo.equalsIgnoreCase("false")) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughArguments());

            return false;
        }

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
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> items = new ArrayList<>(CONFIG.getAllItems());
        items.add("ALL");

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], items, result);
        }

        String item = args[1].toUpperCase();

        if (!items.contains(item)) {
            return List.of();
        }

        if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], List.of("true", "false"), result);
        }

        return result;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public String getDescription() {
        return MESSAGE_CONFIG.getAutoStoreDesc();
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

        if (args.length > 2 && (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return false;
        }

        if (args[1].equalsIgnoreCase("ALL") && args.length <= 2) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughArguments());
            return false;
        }

        if (!args[1].equalsIgnoreCase("ALL") && !CONFIG.getAllItems().contains(args[1].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return false;
        }

        return true;
    }
}