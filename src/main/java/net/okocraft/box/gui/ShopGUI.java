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

package net.okocraft.box.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.milkbowl.vault.economy.Economy;
import net.okocraft.box.Box;
import net.okocraft.box.config.Prices;

/**
 * アイテムの取引GUIの実装
 */
class ShopGUI extends CategoryGUI {
    
    private final Prices prices = plugin.getAPI().getPrices();
    private final Economy economy = Box.getInstance().getEconomy();

    List<ItemStack> available = new ArrayList<>();

    /**
     * コンストラクタ
     *
     * @param player       カテゴリ選択GUIでアイコンをクリックしたプレイヤー
     * @param categoryName 選択されたカテゴリの名前
     * @param quantity     引き出し・預け入れ量
     * @throws IllegalArgumentException カテゴリ名が登録されていないとき。
     */
    ShopGUI(Player player, String categoryName, int quantity) throws IllegalArgumentException {
        super(player, categoryName, Box.getInstance().getAPI().getLayouts().getShopGUITitle().replaceAll("%category-name%", categoryName), quantity);
        
        @SuppressWarnings("serial")
        Map<Integer, ItemStack> pageCommonItems = new HashMap<Integer, ItemStack>() {{
            put(50, applyPlaceholder(layout.getStrage()));
            put(51, applyPlaceholder(layout.getShop()));
            put(52, applyPlaceholder(layout.getCraft()));
        }};
        putPageCommonItems(pageCommonItems);
        available.addAll(categories.getItems(categoryName).stream().map(layout::setShopEntryMeta).collect(Collectors.toList()));        
        refresh();
    }

    void refresh() {
        Map<ItemStack, Integer> stockMap = playerData.getStockAll(getPlayer());
        List<ItemStack> items = new ArrayList<>(available);
        items.removeIf(item -> {
            ItemStack realItem = getRealItem(item);
            double buyPrice = prices.getBuyPrice(realItem);
            double sellPrice = prices.getSellPrice(realItem);

            if ((buyPrice == 0 && sellPrice == 0)) {
                return true;
            }

            if (buyPrice == 0 && stockMap.getOrDefault(realItem, 0) == 0) {
                return true;
            }

            if (sellPrice == 0 && economy.getBalance(getPlayer()) < buyPrice) {
                return true;
            }

            return false;
        });

        clearItems();
        addAllItem(items);
        setPage(getPage());
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        if (event.getSlot() == 50) {
            event.getWhoClicked().openInventory(GUICache.getCache(getPlayer()).getStrageGUICache(getCategoryName(), getQuantity(), getPage()).getInventory());
            return;
        }
        
        if (event.getSlot() == 51) {
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                double money = sellAll();
                if (money > 0) {
                    messages.sendMessage(event.getWhoClicked(), "gui.sell-all", Map.of("%money%", money));
                }
            }
            return;
        }
        
        if (event.getSlot() == 52) {
            event.getWhoClicked().openInventory(GUICache.getCache(getPlayer()).getCraftGUICache(getCategoryName(), getQuantity(), getPage()).getInventory());
            return;
        }

        if (0 <= event.getSlot() && 44 >= event.getSlot()) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) {
                super.onClicked(event);
                return;
            }
            
            if (event.isRightClick()) {
                sell(clickedItem);
            } else {
                buy(clickedItem);
            }

            return;
        }

        super.onClicked(event);
    }

    @Override
    protected void update(ItemStack item) {
        applyPlaceholder(layout.setShopEntryMeta(item));
    }


    @Override
    ItemStack applyPlaceholder(ItemStack item, Map<String, String> placeholder) {
        placeholder.put("%category-name%", getCategoryName());
        placeholder.put("%balance%", String.valueOf(economy.getBalance(getPlayer())));
        
        ItemStack realItem = getRealItem(item);
        if (realItem != null) {
            placeholder.put("%item-name%", getRealItemName(item));
            placeholder.put("%stock%", String.valueOf(playerData.getStock(getPlayer(), realItem)));
            placeholder.put("%buy-price%", String.valueOf(prices.getBuyPrice(realItem)));
            placeholder.put("%sell-price%", String.valueOf(prices.getSellPrice(realItem)));
        }
        return super.applyPlaceholder(item, placeholder);
    }

    
    /**
     * アイテムを売る
     * 
     * @param item 売るアイテム。
     * @return 売ったアイテム数
     */
    private int sell(ItemStack item) {
        Player player = getPlayer();
        ItemStack soldItem = getRealItem(item);
        double price = prices.getSellPrice(soldItem);
        int stock = playerData.getStock(player, soldItem);
        int quantity = getQuantity();

        if (stock == 0 || price == 0) {
            config.playNotEnoughSound(player);
            return 0;
        }
        if (stock < quantity) {
            quantity = stock;
        }

        economy.depositPlayer(player, quantity * price);
        playerData.setStock(player, soldItem, stock - quantity);
        config.playSellSound(player);
        update();
        return quantity;
    }

    /**
     * アイテムを売る
     * 
     * @param item 売るアイテム。
     * @return 買って得たアイテム数
     */
    private int buy(ItemStack item) {
        Player player = getPlayer();
        ItemStack boughtItem = getRealItem(item);
        double price = prices.getBuyPrice(boughtItem);
        double balance = economy.getBalance(player);
        int stock = playerData.getStock(player, boughtItem);
        int quantity = getQuantity();

        if (balance == 0 || price == 0) {
            config.playNotEnoughSound(player);
            return 0;
        }
        
        if (price * quantity > balance) {
            quantity = (int) (balance / price);
        }
        
        if (quantity == 0) {
            config.playNotEnoughSound(player);
            return 0;
        }

        economy.withdrawPlayer(player, quantity * price);
        playerData.setStock(player, boughtItem, stock + quantity);
        config.playBuySound(player);
        update();
        return quantity;
    }

    /**
     * プレイヤーの手持ちのアイテムをすべて売る。
     * 
     * @return 売って得た金額の合計
     */
    private double sellAll() {
        double sum = 0D;
        Player player = getPlayer();
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || itemData.getName(item) == null) {
                continue;
            }
            int amount = item.getAmount();
            amount -= player.getInventory().removeItem(item).values().stream().map(ItemStack::getAmount).mapToInt(Integer::valueOf).sum();
            double price = prices.getSellPrice(item) * amount;
            sum += price;
        }
        if (sum > 0) {
            plugin.getEconomy().depositPlayer(player, sum);
            config.playSellSound(player);
            update();
        }
        return sum;
    }
}