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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.okocraft.box.Box;
import net.okocraft.box.config.CraftRecipes;

/**
 * アイテムの取引GUIの実装
 */
class CraftGUI extends CategoryGUI {

    private final CraftRecipes craftRecipes = plugin.getAPI().getCraftRecipes();

    private final Map<ItemStack, Map<String, Integer>> recipes = new HashMap<>();
    private final Map<ItemStack, Integer> recipeResultAmounts = new HashMap<>();

    private final List<ItemStack> available = new ArrayList<>();

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
        putAvailable(categories.getItems(categoryName).stream().map(layout::setCraftEntryMeta).collect(Collectors.toList()));
        refresh();
    }

    /**
     * ストックが足りなくて作れないアイテム調べ、作れるアイテムをGUIに配置する。
     */
    void refresh() {
        Map<ItemStack, Integer> stockMap = playerData.getStockAll(getPlayer());
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : available) {
            ItemStack realItem = getRealItem(item);

            Map<String, Integer> recipe = recipes.get(realItem);
            if (recipe == null) {
                recipe = getIngredients(realItem);
            }
            boolean isCraftable = true;
            for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
                if (entry.getValue() * getQuantity() > stockMap.getOrDefault(itemData.getItemStack(entry.getKey()), 0)) {
                    isCraftable = false;
                }
            }
            if (isCraftable) {
                items.add(item);
            }
        }
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
            event.getWhoClicked().openInventory(GUICache.getCache(getPlayer()).getShopGUICache(getCategoryName(), getQuantity(), getPage()).getInventory());
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
        placeholder.put("%category-name%", getCategoryName());
        
        ItemStack realItem = getRealItem(item);
        if (realItem != null) {
            placeholder.put("%item-name%", getRealItemName(item));
            placeholder.put("%stock%", String.valueOf(playerData.getStock(getPlayer(), realItem)));
            placeholder.put("%amount%", String.valueOf(getQuantity() * recipeResultAmounts.get(realItem)));
        }
        return super.applyPlaceholder(item, placeholder);
    }

    private void putAvailable(List<ItemStack> items) {
        for (ItemStack item : items) {
            ItemStack realItem = getRealItem(item);
            if (realItem == null) {
                continue;
            }

            Map<String, Integer> ingredients = craftRecipes.getIngredients(realItem);
            int resultAmount = craftRecipes.getResultAmount(realItem);
            
            if (ingredients.isEmpty()) {
                ingredients = getIngredients(realItem);
                if (ingredients.isEmpty()) {
                    continue;
                }
                resultAmount = getCraftResultAmount(realItem);                
            }

            if (resultAmount == 0) {
                continue;
            }

            recipeResultAmounts.put(realItem, resultAmount);
            recipes.put(realItem, ingredients);
            available.add(item);
        }
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
        List<String> ingredientsLore = recipes.get(realItem).entrySet().stream()
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
        Map<String, Integer> stacked = recipes.get(realItem);
        if (stacked == null) {
            return 0;
        }
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
        int add = tempQuantity * recipeResultAmounts.get(realItem);
        playerData.setStock(player, realItem, stock + add);
        config.playCraftSound(player);
        update();
        return add;
    }

    private Recipe getRecipeFor(ItemStack realItem) {
        List<Recipe> recipes = Bukkit.getRecipesFor(realItem);
        recipes.removeIf(recipe -> !(recipe instanceof ShapelessRecipe || recipe instanceof ShapedRecipe));
        if (recipes.isEmpty()) {
            return null;
        }
        return recipes.get(0);
    }

    private int getCraftResultAmount(ItemStack realItem) {
        return getRecipeFor(realItem).getResult().getAmount();
    }

    private Map<String, Integer> getIngredients(ItemStack realItem) {
        Recipe recipe = getRecipeFor(realItem);
        if (recipe == null) {
            return Map.of();
        }
        
        List<ItemStack> ingredients;
        if (recipe instanceof ShapelessRecipe) {
            ingredients = new ArrayList<>(((ShapelessRecipe) recipe).getIngredientList());
        } else {
            ingredients = new ArrayList<>(((ShapedRecipe) recipe).getIngredientMap().values());
        }
        ingredients.removeIf(Objects::isNull);

        Map<String, Integer> result = new HashMap<>();
        for (ItemStack ingredient : ingredients) {
            if (ingredient == null) {
                return Map.of();
            }
            String ingredientName = itemData.getName(ingredient);
            if (ingredientName == null) {
                return Map.of();
            }
            result.put(ingredientName, result.getOrDefault(ingredientName, 0) + ingredient.getAmount());
        }

        return result;
    }
}