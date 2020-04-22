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
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.BaseCommand;

class DepositCommand extends BaseCommand {

    DepositCommand() {
        super(
            "deposit",
            "box.deposit",
            1,
            true,
            "/box deposit [<amount> | <ITEM> [amount] | ALL]",
            new String[] {"d"}
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
            if (itemData.getName(item) == null) {
                messages.sendItemNotFound(sender);
                return false;
            }

            int stock = depositItem((Player) sender, ((Player) sender).getInventory().getHeldItemSlot());
            messages.sendDepositItem(sender, item, item.getAmount(), stock);
            return true;
        }

        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("ALL")) {
                storeAll((Player) sender);
                messages.sendDepositItemAll(sender);
                return true;
            }

            ItemStack item;
            int amount = 1;
            try {
                amount = Integer.parseInt(args[1]);

                item = ((Player) sender).getInventory().getItemInMainHand();
                if (itemData.getName(item) == null) {
                    messages.sendItemNotFound(sender);
                    return false;
                }
            } catch (NumberFormatException e) {
                item = itemData.getItemStack(args[1]);
                if (item == null) {
                    messages.sendItemNotFound(sender);
                    return false;
                }

                if (args.length >= 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            int stock = deposit((Player) sender, item, amount);
            messages.sendWithdrawItem(sender, item, amount, stock);
            return true;
        }

        // will not reach.
        return false;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (itemData.getName(item) == null) {

            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], itemData.getNames(), new ArrayList<>());
            }
    
            if (!itemData.getNames().contains(args[1])) {
                return List.of();
            }
    
            if (args.length == 3) {
                return StringUtil.copyPartialMatches(args[2], List.of("1", "32", "64", "512", "1024"), new ArrayList<>());
            }
    
        } else {
            
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], List.of("ALL", "1", "32", "64", "512", "1024"), new ArrayList<>());
            }

        }
        
        return List.of();
    }

    /**
     * アイテムを預ける。
     *
     * @param item 預けるアイテム
     * @return 預けたあとの在庫数
     */
    private int deposit(Player player, ItemStack item, int amount) {
        ItemStack takenItem = item.clone();
        takenItem.setAmount(amount);
        int nonRemoved = player.getInventory().removeItem(takenItem).values().stream().mapToInt(ItemStack::getAmount)
                .sum();
        int stock = playerData.getStock(player, takenItem);
        if (nonRemoved == amount) {
            return stock;
        }
        playerData.setStock(player, takenItem, stock - nonRemoved + amount);
        return stock - nonRemoved + amount;
    }

    /**
     * アイテムを預ける。
     *
     * @param item 預けるアイテム
     * @return 預けたあとの在庫数
     */
    private int depositItem(Player player, int slot) {
        ItemStack takenItem = player.getInventory().getItem(slot);
        int amount = takenItem.getAmount();
        if (itemData.getName(takenItem) == null) {
            return 0;
        }
        player.getInventory().setItem(slot, null);
        int stock = playerData.getStock(player, takenItem);
        playerData.setStock(player, takenItem, stock + amount);
        return stock + amount;
    }

    /**
     * プレイヤーの手持ちのアイテムをすべてBoxに預ける。
     */
    private void storeAll(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || itemData.getName(item) == null) {
                continue;
            }
            int stock = playerData.getStock(player, item);
            int amount = item.getAmount();
            amount -= player.getInventory().removeItem(item).values().stream().map(ItemStack::getAmount).mapToInt(Integer::valueOf).sum();
            playerData.setStock(player, item, stock + amount);
        }
    }
}