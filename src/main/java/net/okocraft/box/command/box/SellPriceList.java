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

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
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
    public List<String> runTabComplete(CommandSender sender, String[] args) {
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
        return MESSAGE_CONFIG.getSellPriceListDesc();
    }

    @Override
    protected boolean validate(CommandSender sender, String[] args) {
        if (!super.validate(sender, args)) {
            return false;
        }

        if (INSTANCE.getEconomy() == null) {
            sender.sendMessage(MESSAGE_CONFIG.getEconomyIsNull());
            return false;
        }

        return true;
    }
}