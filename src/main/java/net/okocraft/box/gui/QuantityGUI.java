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
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Quantityボタンを実装したGUI
 */
abstract class QuantityGUI extends BackMenuButtomGUI {

    private final int decreaseSlot;
    private final int changeUnitSlot;
    private final int increaseSlot;

    private int quantity = 1;

    public QuantityGUI(Player player, String guiTitle, int GUISize, int quantity, int previousPageSlot,
            int nextPageSlot, int backMenuSlot, int decreaseSlot, int changeUnitSlot, int increaseSlot) {
        super(player, guiTitle, GUISize, previousPageSlot, nextPageSlot, backMenuSlot);
        // super(player, guiTitle, 54, 45, 53);

        this.decreaseSlot = decreaseSlot;
        this.changeUnitSlot = changeUnitSlot;
        this.increaseSlot = increaseSlot;

        @SuppressWarnings("serial")
        Map<Integer, ItemStack> pageCommonItems = new HashMap<>() {
            {
                put(decreaseSlot, layout.getDecrease());
                put(changeUnitSlot, layout.getChangeUnit());
                put(increaseSlot, layout.getIncrease());
            }
        };
        putPageCommonItems(pageCommonItems);
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        int clickedSlot = event.getSlot();

        if (clickedSlot == changeUnitSlot) {
            changeUnit(event.isRightClick());
            return;
        }

        if (clickedSlot == decreaseSlot) {
            setQuantity(quantity - getInventory().getItem(decreaseSlot).getAmount());
            return;
        }

        if (clickedSlot == increaseSlot) {
            setQuantity(quantity + getInventory().getItem(increaseSlot).getAmount());
            return;
        }

        super.onClicked(event);
    }

    @Override
    protected ItemStack applyPlaceholder(ItemStack item, Map<String, String> placeholder) {
        placeholder.put("%quantity%", String.valueOf(quantity));
        return super.applyPlaceholder(item, placeholder);
    }

    @Override
    public void setPage(int page) {
        int decreaseAmount = 1;
        int increaseAmount = 1;
        ItemStack decreaseItem = getInventory().getItem(decreaseSlot);
        ItemStack increaseItem = getInventory().getItem(increaseSlot);
        if (decreaseItem != null) {
            decreaseAmount = decreaseItem.getAmount();
        }
        if (increaseItem != null) {
            increaseAmount = increaseItem.getAmount();
        }
        
        super.setPage(page);

        getInventory().getItem(decreaseSlot).setAmount(decreaseAmount);
        getInventory().getItem(increaseSlot).setAmount(increaseAmount);
    }

    private void changeUnit(boolean isRightClick) {
        double multiplier = isRightClick ? 0.5 : 2;
        int result = Math.min(64, Math.max(1, (int) (getInventory().getItem(decreaseSlot).getAmount() * multiplier)));
        getInventory().getItem(decreaseSlot).setAmount(result);
        getInventory().getItem(increaseSlot).setAmount(result);
    }

    /**
     * 取引量を変える。
     *
     * @param newQuantity 新しい取引量
     */
    void setQuantity(int newQuantity) {
        // force 1 < newQnatity < maxQuantity
        newQuantity = Math.max(1, Math.min(newQuantity, config.getMaxQuantity()));

        if (quantity < newQuantity) {
            config.playIncreaseUnitSound(getPlayer());
        } else if (quantity > newQuantity) {
            config.playDecreaseUnitSound(getPlayer());
        } else {
            return;
        }
        quantity = newQuantity;
        setPage(getPage());
    }

    /**
     * 取引量を取得する。
     * 
     * @return 取引量
     */
    public int getQuantity() {
        return quantity;
    }
}