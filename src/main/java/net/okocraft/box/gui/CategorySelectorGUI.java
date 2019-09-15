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

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.box.Box;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.MessageConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CategorySelectorGUI implements Listener {

    @Nullable
    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();
    private static final MessageConfig MESSAGE_CONFIG = INSTANCE.getMessageConfig();

    private static final NamespacedKey CATEGORY_SELECTOR_KEY = new NamespacedKey(INSTANCE, "categoryselector");
    private static final NamespacedKey CATEGORY_NAME_KEY = new NamespacedKey(INSTANCE, "categoryname");

    public static final Inventory GUI = Bukkit.createInventory(null, 54, CONFIG.getCategorySelectionGuiName());

    private static List<Integer> flameSlots = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46,
            47, 48, 49, 50, 51, 52, 53);

    @Nullable
    private static CategorySelectorGUI categorySelector;

    /**
     * コンストラクタ
     */
    private CategorySelectorGUI() {
        initGUI();
    }

    public static void initGUI() {
        GUI.clear();
        ItemStack flame = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta flameMeta = flame.getItemMeta();
        flameMeta.setDisplayName("§r");
        flameMeta.getPersistentDataContainer().set(CATEGORY_SELECTOR_KEY, PersistentDataType.INTEGER, 1);
        flame.setItemMeta(flameMeta);
        flameSlots.forEach(slot -> GUI.setItem(slot, flame));

        List<ItemStack> itemList = CONFIG.getCategories().values().stream().map(category -> category.getIconItem())
                .collect(Collectors.toList());
        GUI.addItem(itemList.toArray(new ItemStack[itemList.size()]));
    }

    /**
     * リスナーを動かす。カテゴリーGUIと違ってカテゴリー選択GUIはonEnableのときから常にリスナーをオンにしておく。
     */
    public static void startListener() {
        if (categorySelector != null) {
            return;
        }
        categorySelector = new CategorySelectorGUI();
        Bukkit.getPluginManager().registerEvents(categorySelector, INSTANCE);
    }

    /**
     * リスナーを止める。
     */
    public static void stopListener() {
        if (categorySelector == null) {
            return;
        }
        HandlerList.unregisterAll(categorySelector);
        categorySelector = null;
    }

    /**
     * リスナーを再起動する。
     */
    public static void restartListener() {
        stopListener();
        startListener();
    }

    /**
     * カテゴリ選択GUIへのクリックを検知して、適切なカテゴリGUIに遷移させる。
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(@NotNull InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        InventoryAction action = event.getAction();
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || inventory.getItem(0) == null || !GUI.getItem(0).isSimilar(inventory.getItem(0))) {
            return;
        }
        event.setCancelled(true);

        if (CONFIG.getDisabledWorlds().contains(event.getWhoClicked().getWorld().getName())) {
            player.sendMessage(MESSAGE_CONFIG.getDisabledWorld());
            return;
        }

        int clickedSlot = event.getSlot();
        ItemStack clickedItem = inventory.getItem(clickedSlot);

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (action == InventoryAction.NOTHING || inventory.getType() == InventoryType.PLAYER) {
            return;
        }

        if (flameSlots.contains(clickedSlot)) {
            return;
        }

        // NOTE: NPE はItemStackがAIRの場合のみしか起こらない。
        String categoryName = clickedItem.getItemMeta().getPersistentDataContainer().get(CATEGORY_NAME_KEY,
                PersistentDataType.STRING);

        player.closeInventory();

        new CategoryGUI(player, categoryName, 1);

        return;
    }
}