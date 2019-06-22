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

package net.okocraft.box.command;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.val;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import net.okocraft.box.Box;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.database.Database;

public class BoxTabCompleter implements TabCompleter {
    private Database database;
    private GeneralConfig generalConfig;

    public BoxTabCompleter(Database database) {
        val instance = Box.getInstance();

        this.database = database;
        this.generalConfig = instance.getGeneralConfig();

        Optional.ofNullable(instance.getCommand("box")).ifPresent(cmd -> cmd.setTabCompleter(this));

        Optional.ofNullable(instance.getCommand("boxadmin")).ifPresent(cmd -> cmd.setTabCompleter(this));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        val resultList = new ArrayList<String>();

        if (command.getName().equalsIgnoreCase("boxadmin")) {
            return onTabCompleteBoxAdminCommand(sender, resultList, args);
        }

        if (command.getName().equalsIgnoreCase("box")) {
            return onTabCompleteBoxCommand(sender, resultList, args);
        }

        return resultList;
    }

    public List<String> onTabCompleteBoxCommand(CommandSender sender, List<String> resultList, String[] args) {
        val subCommands = new ArrayList<String>();

        if (sender.hasPermission("box.version")) {
            subCommands.add("version");
        }

        if (sender.hasPermission("box.autostore") || sender.hasPermission("box.autostore.*")) {
            subCommands.add("autostore");
        }

        if (sender.hasPermission("box.autostorelist")) {
            subCommands.add("autostorelist");
        }

        if (sender.hasPermission("box.give") || sender.hasPermission("box.give.*")) {
            subCommands.add("give");
        }

        if (sender.hasPermission("box.sell") || sender.hasPermission("box.sell.*")) {
            subCommands.add("sell");
        }

        if (sender.hasPermission("box.sellprice")) {
            subCommands.add("sellprice");
        }

        if (sender.hasPermission("box.sellpricelist")) {
            subCommands.add("sellpricelist");
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subCommands, resultList);
        }

        val subCommand = args[0].toLowerCase();

        if (!subCommands.contains(subCommand)) {
            return resultList;
        }

        val allItems = generalConfig.getAllItems();

        val allItemsAutostore = sender.hasPermission("box.autostore.*") ? new ArrayList<>(allItems)
                : allItems.stream().filter(itemName -> sender.hasPermission("box.autostore." + itemName))
                        .collect(Collectors.toList());
        allItemsAutostore.add("ALL");

        val allItemsSell = sender.hasPermission("box.autostore.*") ? new ArrayList<>(allItems)
                : allItems.stream().filter(itemName -> sender.hasPermission("box.sell." + itemName))
                        .collect(Collectors.toList());

        int autoStoreMaxPage = allItemsAutostore.size() / 9;
        autoStoreMaxPage = (allItemsAutostore.size() % 9 == 0) ? autoStoreMaxPage : autoStoreMaxPage + 1;
        int sellMaxPage = allItemsSell.size() / 9;
        sellMaxPage = (allItemsSell.size() % 9 == 0) ? sellMaxPage : sellMaxPage + 1;

        val players = new ArrayList<>(database.getPlayersMap().values());

        if (args.length == 2) {
            switch (subCommand) {
            case "autostorelist":
                return StringUtil.copyPartialMatches(args[1],
                        IntStream.rangeClosed(1, autoStoreMaxPage).boxed().map(String::valueOf).collect(Collectors.toList()),
                        resultList);
            case "autostore":
                return StringUtil.copyPartialMatches(args[1], allItemsAutostore, resultList);
            case "give":
                return StringUtil.copyPartialMatches(args[1], players, resultList);
            case "sell":
                return StringUtil.copyPartialMatches(args[1], allItemsSell, resultList);
            case "sellprice":
                return StringUtil.copyPartialMatches(args[1], allItemsSell, resultList);
            case "sellpricelist":
                return StringUtil.copyPartialMatches(args[1],
                        IntStream.rangeClosed(1, sellMaxPage).boxed().map(String::valueOf).collect(Collectors.toList()),
                        resultList);
            }
        }

        switch (subCommand) {
        case "autostore":
            if (!allItemsAutostore.contains(args[1].toUpperCase()) && !args[1].equalsIgnoreCase("all")) {
                return resultList;
            }

            break;
        case "sell":
            if (!allItemsSell.contains(args[1].toUpperCase())) {
                return resultList;
            }

            break;
        case "give":
            if (!players.contains(args[1])) {
                return resultList;
            }

            break;
        }

        val allItemsGive = sender.hasPermission("box.give.*") ? generalConfig.getAllItems()
                : generalConfig.getAllItems().stream().filter(itemName -> sender.hasPermission("box.give." + itemName))
                        .collect(Collectors.toList());

        if (args.length == 3) {
            switch (subCommand) {
            case "autostore":
                return StringUtil.copyPartialMatches(args[2], List.of("true", "false"), resultList);
            case "give":
                return StringUtil.copyPartialMatches(args[2], allItemsGive, resultList);
            case "sell":
                return StringUtil.copyPartialMatches(args[2], List.of("1", "10", "100", "1000"), resultList);
            }
        }

        return resultList;
    }

    public List<String> onTabCompleteBoxAdminCommand(CommandSender sender, List<String> resultList, String[] args) {
        if (!sender.hasPermission("box.admin")) {
            return resultList;
        }

        val subCommands = List.of("set", "give", "take", "reload", "test", "autostore", "autostorelist", "database");

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subCommands, resultList);
        }

        val subCommand = args[0].toLowerCase();

        if (!subCommands.contains(subCommand)) {
            return resultList;
        }

        val playerList = database.getPlayersMap().values().parallelStream().collect(Collectors.toList());

        if (subCommand.equalsIgnoreCase("database")) {
            return onTabCompleteDatabaseCommand(sender, playerList, resultList, args);
        }

        if (args.length == 2) {
            switch (subCommand) {
            case "set":
            case "autostore":
            case "autostorelist":
            case "take":
            case "give":
                return StringUtil.copyPartialMatches(args[1], playerList, resultList);
            }
        }

        if (!playerList.contains(args[1])) {
            return resultList;
        }

        val allItems = new ArrayList<>(generalConfig.getAllItems());
        val allItemsAndAll = new ArrayList<>(allItems);
        allItemsAndAll.add("ALL");

        int maxPage = allItems.size() / 9;
        maxPage = (allItems.size() % 9 == 0) ? maxPage : maxPage + 1;
        val pages = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf).collect(Collectors.toList());

        if (args.length == 3) {
            switch (subCommand) {
            case "set":
            case "take":
            case "give":
                return StringUtil.copyPartialMatches(args[2], allItems, resultList);
            case "autostore":
                return StringUtil.copyPartialMatches(args[2], allItemsAndAll, resultList);
            case "autostorelist":
                return StringUtil.copyPartialMatches(args[2], pages, resultList);
            }
        }

        switch (subCommand) {
        case "set":
        case "take":
        case "give":
            if (!allItems.contains(args[2].toUpperCase())) {
                return resultList;
            }
        case "autostore":
            if (!allItemsAndAll.contains(args[2].toUpperCase())) {
                return resultList;
            }
        }

        if (args.length == 4) {
            switch (subCommand) {
            case "set":
            case "give":
            case "take":
                return StringUtil.copyPartialMatches(args[3], List.of("1", "10", "100", "1000", "10000"), resultList);
            case "autostore":
                return StringUtil.copyPartialMatches(args[3], List.of("true", "false"), resultList);
            }
        }

        return resultList;
    }

    public List<String> onTabCompleteDatabaseCommand(CommandSender sender, List<String> playerList,
            List<String> resultList, String[] args) {

        val databaseSubCommands = List.of("addplayer", "removeplayer", "existplayer", "set", "get", "addcolumn",
                "dropcolumn", "getcolumnmap", "getplayersmap", "resetconnection");

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], databaseSubCommands, resultList);
        }

        if (!databaseSubCommands.contains(args[1].toLowerCase())) {
            return resultList;
        }

        val columnList = new ArrayList<>(database.getColumnMap().keySet());

        if (args.length == 3) {
            switch (args[1].toLowerCase()) {
            case "addplayer":
                return null;
            case "removeplayer":
            case "existplayer":
                return StringUtil.copyPartialMatches(args[2], playerList, resultList);
            case "set":
            case "dropcolumn":
            case "get":
                return StringUtil.copyPartialMatches(args[2], columnList, resultList);
            case "addcolumn":
                return StringUtil.copyPartialMatches(args[2], Collections.singletonList("<new_column_name>"),
                        resultList);
            }
        }

        if (List.of("removeplayer", "existplayer").contains(args[1].toLowerCase()) && !playerList.contains(args[2])) {
            return resultList;
        }

        if (List.of("set", "get", "dropcolumn").contains(args[1].toLowerCase()) && !columnList.contains(args[2])) {
            return resultList;
        }

        val sqlTypeList = List.of("TEXT", "INTEGER", "NONE", "NUMERIC", "REAL");
        if (args.length == 4) {
            switch (args[1].toLowerCase()) {
            case "set":
            case "get":
                return StringUtil.copyPartialMatches(args[3], playerList, resultList);
            case "addcolumn":
                return StringUtil.copyPartialMatches(args[3], sqlTypeList, resultList);
            }
        }

        if (List.of("get", "set").contains(args[1].toLowerCase()) && !playerList.contains(args[3])) {
            return resultList;
        }

        if (args[1].equalsIgnoreCase("addcolumn") && !sqlTypeList.contains(args[3].toUpperCase())) {
            return resultList;
        }

        if (args.length == 5) {
            switch (args[1].toLowerCase()) {
            case "set":
                return StringUtil.copyPartialMatches(args[4], Collections.singletonList("<value>"), resultList);
            case "addcolumn":
                return StringUtil.copyPartialMatches(args[4], List.of("<default_value>", "null"), resultList);
            }
        }

        return resultList;

    }
}
