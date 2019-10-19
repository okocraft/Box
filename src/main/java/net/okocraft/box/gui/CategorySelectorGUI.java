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
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.box.Box;
import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Config;
import net.okocraft.box.config.Messages;
import net.okocraft.box.config.Categories.Category;
import org.jetbrains.annotations.Nullable;

public final class CategorySelectorGUI implements Listener, InventoryHolder {

    @Nullable
    private static final Box plugin = Box.getInstance();
    private static final NamespacedKey CATEGORY_NAME_KEY = new NamespacedKey(plugin, "categoryname");
    private static final List<Integer> flameSlots = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46,
            47, 48, 49, 50, 51, 52, 53);

    private static CategorySelectorGUI categorySelector = new CategorySelectorGUI();
    public static Inventory GUI = initGUI();

    /**
     * コンストラクタ禁止
     */
    private CategorySelectorGUI() {
    }

    public static Inventory initGUI() {
        GUI = Bukkit.createInventory(categorySelector, 54, Config.getCategorySelectionConfig().getName());
        ItemStack flame = new ItemStack(Material.BLACK_STAINED_GLASS_PANE); 
        ItemMeta flameMeta = flame.getItemMeta();
        flameMeta.setDisplayName("§r");
        flame.setItemMeta(flameMeta);
        flameSlots.forEach(slot -> GUI.setItem(slot, flame));

        List<ItemStack> itemList = Categories.getInstance().getAllCategories().stream().map(Category::getIcon)
                .collect(Collectors.toList());
        GUI.addItem(itemList.toArray(new ItemStack[itemList.size()]));
        return GUI;
    }

    @Override
    public Inventory getInventory() {
        return GUI;
    }

    /**
     * リスナーを動かす。カテゴリーGUIと違ってカテゴリー選択GUIはonEnableのときから常にリスナーをオンにしておく。
     */
    public static void startListener() {
        Bukkit.getPluginManager().registerEvents(categorySelector, plugin);
    }

    /**
     * リスナーを止める。
     */
    public static void stopListener() {
        HandlerList.unregisterAll(categorySelector);
    }

    /**
     * リスナーを再起動する。
     */
    public static void restartListener() {
        stopListener();
        categorySelector = new CategorySelectorGUI();
        GUI = initGUI();
        startListener();
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getView().getTopInventory().getHolder() != this) {
            return;
        }
        if (Config.getConfig().getDisabledWorlds().contains(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            Messages.getInstance().sendMessage(event.getPlayer(), "command.general.error.in-disabled-world");
        }
    }

    /**
     * カテゴリ選択GUIへのクリックを検知して、適切なカテゴリGUIに遷移させる。
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() != this) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryAction action = event.getAction();
        event.setCancelled(true);

        if (Config.getConfig().getDisabledWorlds().contains(event.getWhoClicked().getWorld())) {
            Messages.getInstance().sendMessage(player, "command.general.error.in-disabled-world");
            return;
        }

        int clickedSlot = event.getSlot();
        if (clickedSlot == -999) {
            return;
        }
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

        new CategoryGUI(player, Categories.getInstance().getCategory(categoryName), 1);
    }
}