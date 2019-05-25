package com.github.okocraft.box.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.okocraft.box.ConfigManager;
import com.github.okocraft.box.Box;
import com.github.okocraft.box.database.Database;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import lombok.val;

public class BoxTabCompleter implements TabCompleter {

    private Database database;
    private Box instance;
    private ConfigManager configManager;

    public BoxTabCompleter(Database database) {
        this.database = database;
        this.instance = Box.getInstance();
        configManager = instance.getConfigManager();

        instance.getCommand("box").setTabCompleter(this);
        instance.getCommand("boxadmin").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> resultList = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("boxadmin")) {
            if (!sender.hasPermission("box.admin"))
                return resultList;

            List<String> subCommands = Arrays.asList("set", "give", "take", "reload", "test", "migrate", "autostore",
                    "database");
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], subCommands, resultList);
            }
            final String subCommand = args[0].toLowerCase();
            if (!subCommands.contains(subCommand))
                return resultList;

            List<String> playerList = database.getPlayersMap().values().parallelStream().collect(Collectors.toList());
            List<String> databaseSubCommands = Arrays.asList("addplayer", "removeplayer", "existplayer", "set", "get",
                    "addcolumn", "dropcolumn", "getcolumnmap", "getplayersmap");

            if (subCommand.equalsIgnoreCase("database")) {

                if (args.length == 2) {
                    return StringUtil.copyPartialMatches(args[1], databaseSubCommands, resultList);
                }

                if (databaseSubCommands.contains(args[1].toLowerCase()))
                    return resultList;

                List<String> columnList = new ArrayList<>(database.getColumnMap().keySet());
                if (args.length == 3) {
                    switch (args[1].toLowerCase()) {
                    case "removeplayer":
                        return StringUtil.copyPartialMatches(args[2], playerList, resultList);
                    case "existplayer":
                        return StringUtil.copyPartialMatches(args[2], playerList, resultList);
                    case "set":
                        return StringUtil.copyPartialMatches(args[2], columnList, resultList);
                    case "get":
                        return StringUtil.copyPartialMatches(args[2], columnList, resultList);
                    case "addcolumn":
                        return StringUtil.copyPartialMatches(args[2], Arrays.asList("<new_column_name>"), resultList);
                    case "dropcolumn":
                        return StringUtil.copyPartialMatches(args[2], columnList, resultList);
                    }
                }

                if (Arrays.asList("removeplayer", "existplayer").contains(args[1].toLowerCase()))
                    if (!playerList.contains(args[2]))
                        return resultList;

                if (Arrays.asList("set", "get", "dropcolumn").contains(args[1].toLowerCase()))
                    if (!columnList.contains(args[2]))
                        return resultList;

                List<String> sqlTypeList = Arrays.asList("TEXT", "INTEGER", "NONE", "NUMERIC", "REAL");
                if (args.length == 4) {
                    switch (args[1].toLowerCase()) {
                    case "set":
                        return StringUtil.copyPartialMatches(args[3], playerList, resultList);
                    case "get":
                        return StringUtil.copyPartialMatches(args[3], playerList, resultList);
                    case "addcolumn":
                        return StringUtil.copyPartialMatches(args[3], sqlTypeList, resultList);
                    }
                }

                if (Arrays.asList("get", "set").contains(args[1].toLowerCase()))
                    if (!playerList.contains(args[3]))
                        return resultList;

                if (args[1].equalsIgnoreCase("addcolumn"))
                    if (!sqlTypeList.contains(args[3].toUpperCase()))
                        return resultList;

                if (args.length == 5) {
                    switch (args[1].toLowerCase()) {
                    case "set":
                        return StringUtil.copyPartialMatches(args[4], Arrays.asList("<value>"), resultList);
                    case "addcolumn":
                        return StringUtil.copyPartialMatches(args[4], Arrays.asList("<default_value>", "null"),
                                resultList);
                    }
                }

                return resultList;
            }

            if (args.length == 2) {

                switch (subCommand) {
                case "set":
                    return StringUtil.copyPartialMatches(args[1], playerList, resultList);
                case "give":
                    return StringUtil.copyPartialMatches(args[1], playerList, resultList);
                case "take":
                    return StringUtil.copyPartialMatches(args[1], playerList, resultList);
                case "autostore":
                    return StringUtil.copyPartialMatches(args[1], playerList, resultList);
                }
            }

            if (!playerList.contains(args[1]))
                return resultList;

            List<String> allItems = configManager.getAllItems();
            if (args.length == 3) {
                switch (subCommand) {
                case "set":
                    return StringUtil.copyPartialMatches(args[2], allItems, resultList);
                case "give":
                    return StringUtil.copyPartialMatches(args[2], allItems, resultList);
                case "take":
                    return StringUtil.copyPartialMatches(args[2], allItems, resultList);
                case "autostore":
                    return StringUtil.copyPartialMatches(args[2], allItems, resultList);
                }
            }

            if (!allItems.contains(args[2].toUpperCase()))
                return resultList;

            if (args.length == 4) {
                switch (subCommand) {
                case "set":
                    return StringUtil.copyPartialMatches(args[2], Arrays.asList("1", "10", "100", "1000", "10000"),
                            resultList);
                case "give":
                    return StringUtil.copyPartialMatches(args[2], Arrays.asList("1", "10", "100", "1000", "10000"),
                            resultList);
                case "take":
                    return StringUtil.copyPartialMatches(args[2], Arrays.asList("1", "10", "100", "1000", "10000"),
                            resultList);
                case "autostore":
                    return StringUtil.copyPartialMatches(args[2], Arrays.asList("true", "false"), resultList);
                }
            }

            return resultList;
        }

        if (command.getName().equalsIgnoreCase("box")) {

            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("box.version"))
                subCommands.add("version");
            if (sender.hasPermission("box.autostore") || sender.hasPermission("box.autostore.*"))
                subCommands.add("autostore");
            if (sender.hasPermission("box.give") || sender.hasPermission("box.give.*"))
                subCommands.add("give");
            
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], subCommands, resultList);
            }

            val subCommand = args[1].toLowerCase();

            switch(subCommand) {
                case "autostore":
                if (!sender.hasPermission("box.autostore") && !sender.hasPermission("box.autostore.*"))
                    return resultList;
                case "give":
                if (!sender.hasPermission("box.give"))
                    return resultList;
            }

            List<String> allItemsAutostore = (sender.hasPermission("box.autostore.*"))
                ? configManager.getAllItems().stream().filter(itemName -> sender.hasPermission("box.autostore." + itemName)).collect(Collectors.toList())
                : configManager.getAllItems();

            List<String> players = database.getPlayersMap().values().stream().collect(Collectors.toList());
            if (args.length == 2) {
                switch(subCommand) {
                    case "autostore":
                        StringUtil.copyPartialMatches(args[1], allItemsAutostore, resultList);
                    case "give":
                        StringUtil.copyPartialMatches(args[1], players, resultList);
                }
            }

            switch(subCommand) {
                case "autostore":
                    if (!allItemsAutostore.contains(args[1].toUpperCase()))
                        return resultList;
                case "give":
                    if (!players.contains(args[1]))
                        return resultList;
            }

            List<String> allItemsGive = (sender.hasPermission("box.give.*"))
                ? configManager.getAllItems().stream().filter(itemName -> sender.hasPermission("box.autostore." + itemName)).collect(Collectors.toList())
                : configManager.getAllItems();
            if (args.length == 3) {
                switch(subCommand) {
                    case "autostore":
                        StringUtil.copyPartialMatches(args[2], Arrays.asList("true", "false"), resultList);
                    case "give":
                        StringUtil.copyPartialMatches(args[2], allItemsGive, resultList);
                }
            }
        }
        return resultList;
    }
}