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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.box.Box;

public final class CategorySelectorGUI extends BaseGUI implements Clickable {

    private final NamespacedKey categoryNameKey = new NamespacedKey(plugin, "categoryname");
    private final List<Integer> frameSlots = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46,
            47, 48, 49, 50, 51, 52, 53);

    public CategorySelectorGUI() {
        super(54, Box.getInstance().getAPI().getLayouts().getCategorySelectorGUITitle());
        
        
        ItemStack frame = layout.getFrame();
        @SuppressWarnings("serial")
        Map<Integer, ItemStack> pageCommonItems = new HashMap<>() {{
            for (int frameSlot : frameSlots) {
                put(frameSlot, frame);
            }
        }};
        putPageCommonItems(pageCommonItems);

        List<ItemStack> itemList = categories.getCategories().stream().map(categories::getIcon)
                .collect(Collectors.toList());
        addAllItem(itemList);
        setPage(getPage()); 
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem.getItemMeta() == null) {
            return;
        }

        String categoryName = getCategoryName(clickedItem);
        if (categoryName != null) {
            Player who = (Player) event.getWhoClicked();
            config.playGUIOpenSound(who);
            event.getWhoClicked().openInventory(GUICache.getCache(who).getStrageGUICache(categoryName, 1, 1).getInventory());
        }
    }

    private String getCategoryName(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(categoryNameKey,
                PersistentDataType.STRING);
    }

    @Override
    protected ItemStack applyPlaceholder(ItemStack item, Map<String, String> placeholder) {
        String categoryName = getCategoryName(item);
        if (categoryName != null) {
            placeholder.put("%category-name%", categoryName);
            placeholder.put("%display-name%", categories.getDisplayName(categoryName));
        }
        return super.applyPlaceholder(item, placeholder);
    }
}