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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.BaseCommand;

class WithdrawCommand extends BaseCommand {

    WithdrawCommand() {
        super(
            "withdraw",
            "box.withdraw",
            2,
            true,
            "/box withdraw <ITEM> [amount]",
            new String[] {"w"}
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        String itemName = args[1].toUpperCase(Locale.ROOT);
        if (!categories.getAllItems().contains(itemName)) {
            messages.sendItemNotFound(sender);
            return false;
        }
        ItemStack item = itemData.getItemStack(itemName);
        if (item == null) {
            messages.sendItemNotFound(sender);
            return false;
        }

        int stockBefore = playerData.getStock((Player) sender, item);

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.min(stockBefore, Math.max(1, Integer.parseInt(args[2])));
            } catch (NumberFormatException ignored) {
            }
        }

        if (stockBefore < amount) {
            messages.sendNotEnoughStock(sender);
            return false;
        }
        
        int stock = withdraw((Player) sender, item, amount);
        if (stockBefore == stock) {
            messages.sendInventoryIsFull(sender);
        } else {
            messages.sendWithdrawItem(sender, item, stockBefore - stock, stock);
        }
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        Set<String> itemSet = categories.getAllItems();
        itemSet.retainAll(itemData.getNames());
        List<String> itemList = new ArrayList<>(itemSet);
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], itemList, new ArrayList<>());
        }

        if (itemSet.contains(args[1].toUpperCase(Locale.ROOT))) {
            return List.of();
        }

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], List.of("1", "32", "64", "512", "1024"), new ArrayList<>());
        }

        return List.of();
    }

    /**
     * アイテムを引き出す。
     *
     * @param item 引き出すアイテム
     * @return 引き出したあとの在庫数
     */
    private int withdraw(Player player, ItemStack item, int amount) {
        ItemStack givenItem = item.clone();
        givenItem.setAmount(1);
        int stock = playerData.getStock(player, givenItem);
        if (stock == 0) {
            return 0;
        }
        if (amount > stock) {
            amount = stock;
        }
        givenItem.setAmount(amount);
        int nonAdded = addItem(player.getInventory(), givenItem).values().stream()
                .mapToInt(ItemStack::getAmount).sum();
        givenItem.setAmount(1);
        playerData.setStock(player, givenItem, stock + nonAdded - amount);
        return stock + nonAdded - amount;
    }

    private int firstPartial(Inventory inv, ItemStack item) {
        if (item == null) {
            return -1;
        }
        ItemStack[] inventory = inv.getStorageContents();
        ItemStack filteredItem = item.clone();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack cItem = inventory[i];
            if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(filteredItem)) {
                return i;
            }
        }
        return -1;
    }

    private Map<Integer, ItemStack> addItem(Inventory inv, ItemStack... items) {
        Objects.requireNonNull(items, "Item cannot be null");
        Map<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();

        /* TODO: some optimization
         *  - Create a 'firstPartial' with a 'fromIndex'
         *  - Record the lastPartial per Material
         *  - Cache firstEmpty result
         */

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            while (true) {
                // Do we already have a stack of it?
                
                int firstPartial = firstPartial(inv, item);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    int firstFree = inv.firstEmpty();

                    if (firstFree == -1) {
                        // No space at all!
                        leftover.put(i, item);
                        break;
                    } else {
                        // More than a single stack!
                        if (item.getAmount() > item.getMaxStackSize()) {
                            ItemStack stack = item.clone();
                            stack.setAmount(item.getMaxStackSize());
                            inv.setItem(firstFree, stack);
                            item.setAmount(item.getAmount() - item.getMaxStackSize());
                        } else {
                            // Just store it
                            inv.setItem(firstFree, item);
                            break;
                        }
                    }
                } else {
                    // So, apparently it might only partially fit, well lets do just that
                    ItemStack partialItem = inv.getItem(firstPartial);

                    int amount = item.getAmount();
                    int partialAmount = partialItem.getAmount();
                    int maxAmount = partialItem.getMaxStackSize();

                    // Check if it fully fits
                    if (amount + partialAmount <= maxAmount) {
                        partialItem.setAmount(amount + partialAmount);
                        // To make sure the packet is sent to the client
                        inv.setItem(firstPartial, partialItem);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(maxAmount);
                    // To make sure the packet is sent to the client
                    inv.setItem(firstPartial, partialItem);
                    item.setAmount(amount + partialAmount - maxAmount);
                }
            }
        }
        return leftover;
    }
}