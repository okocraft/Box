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
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.okocraft.box.Box;
import net.okocraft.box.util.CraftRecipes;

/**
 * アイテムの取引GUIの実装
 */
class CraftGUI extends CategoryGUI {

    /**
     * コンストラクタ
     *
     * @param player       カテゴリ選択GUIでアイコンをクリックしたプレイヤー
     * @param categoryName 選択されたカテゴリの名前
     * @param quantity     引き出し・預け入れ量
     * @throws IllegalArgumentException カテゴリ名が登録されていないとき。
     */
    public CraftGUI(Player player, String categoryName, int quantity) throws IllegalArgumentException {
        super(player, categoryName, Box.getInstance().getAPI().getLayouts().getCraftGUITitle().replaceAll("%category-name%", categoryName), quantity);
        
        @SuppressWarnings("serial")
        Map<Integer, ItemStack> pageCommonItems = new HashMap<Integer, ItemStack>() {{
            put(50, applyPlaceholder(layout.getStrage()));
            put(51, applyPlaceholder(layout.getShop()));
            put(52, applyPlaceholder(layout.getCraft()));
        }};
        putPageCommonItems(pageCommonItems);
        addAllItem(categories.getItems(categoryName).stream().map(layout::setCraftEntryMeta).collect(Collectors.toList()));
        filterUnavailable();
        setPage(getPage());
    }
    
    @Override
    public void onClicked(InventoryClickEvent event) {
        if (event.getSlot() == 50) {
            event.getWhoClicked().openInventory(new StrageGUI(getPlayer(), getCategoryName(), getQuantity()).getInventory());
            return;
        }
        
        if (event.getSlot() == 51) {
            event.getWhoClicked().openInventory(new ShopGUI(getPlayer(), getCategoryName(), getQuantity()).getInventory());
            return;
        }

        if (0 <= event.getSlot() && 44 >= event.getSlot()) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) {
                super.onClicked(event);
                return;
            }

            if (event.isRightClick()) {
                return;
            }

            craft(clickedItem);
            return;
        }

        super.onClicked(event);
    }

    @Override
    protected ItemStack applyPlaceholder(ItemStack item, Map<String, String> placeholder) {
        ItemStack realItem = Objects.requireNonNullElse(getRealItem(item), item);
        placeholder.put("%item-name%", Objects.requireNonNullElse(getRealItemName(item), item.getType().toString()));
        placeholder.put("%category-name%", getCategoryName());
        placeholder.put("%stock%", String.valueOf(playerData.getStock(getPlayer(), realItem)));
        placeholder.put("%amount%", String.valueOf(getQuantity() * CraftRecipes.getResultAmount(realItem)));
        return super.applyPlaceholder(item, placeholder);
    }

    /**
     * ストックが足りなくて作れないアイテムを削除する。
     */
    private void filterUnavailable() {
        List<ItemStack> items = new ArrayList<>(getItems());
        List<ItemStack> available = CraftRecipes.filterUnavailable(getPlayer(), items, getQuantity());
        clearItems();
        addAllItem(available);
    }
    
    @Override
    protected void update(ItemStack item) {
        applyPlaceholder(layout.setCraftEntryMeta(item));
        ItemStack realItem = getRealItem(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || realItem == null) {
            return;
        }

        List<String> lore = item.getItemMeta().getLore();
        List<String> ingredientsLore = CraftRecipes.getIngredient(realItem).entrySet().stream()
                .map(entry -> ChatColor.translateAlternateColorCodes('&', layout.getMaterialsPlaceholderFormat()
                        .replaceAll("%material%", entry.getKey())
                        .replaceAll("%material-stock%", String.valueOf(
                                playerData.getStock(getPlayer(), itemData.getItemStack(entry.getKey()))))
                        .replaceAll("%amount%", String.valueOf(entry.getValue() * getQuantity())))
                ).collect(Collectors.toList());
        for (int i = lore.size() - 1; i >= 0; i--) {
            if (lore.get(i).contains("%materials%")) {
                lore.remove(i);
                lore.addAll(i, ingredientsLore);
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * アイテムをクラフトする。引数にはGUIでクリックしたアイテムを取る。
     * 
     * @param item 作ろうとしているアイテムのGUI表示
     * @return 作られたアイテムの数
     */
    private int craft(ItemStack item) {
        ItemStack realItem = getRealItem(item);
        if (realItem == null) {
            return 0;
        }
        Map<String, Integer> stacked = CraftRecipes.getIngredient(realItem);
        int tempQuantity = getQuantity();
        Player player = getPlayer();
        for (Map.Entry<String, Integer> entry : stacked.entrySet()) {
            // 材料の在庫から、作れるアイテム数を割り出す
            int materialStock = playerData.getStock(player, itemData.getItemStack(entry.getKey()));
            tempQuantity = Math.min(entry.getValue() * tempQuantity, materialStock) / entry.getValue();
        }
        if (tempQuantity == 0) {
            config.playNotEnoughSound(player);
            return 0;
        }
        for (Map.Entry<String, Integer> entry : stacked.entrySet()) {
            ItemStack ingredient = itemData.getItemStack(entry.getKey());
            int stock = playerData.getStock(player, ingredient);
            playerData.setStock(player, ingredient, stock - tempQuantity * entry.getValue());
        }
        int stock = playerData.getStock(player, realItem);
        int add = tempQuantity * CraftRecipes.getResultAmount(realItem);
        playerData.setStock(player, realItem, stock + add);
        update();
        return add;
    }
}