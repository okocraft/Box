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
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
/**
 * アイテムの取引GUIの実装
 */
class StrageGUI extends CategoryGUI {

    /**
     * コンストラクタ
     *
     * @param player       カテゴリ選択GUIでアイコンをクリックしたプレイヤー
     * @param categoryName 選択されたカテゴリの名前
     * @param quantity     引き出し・預け入れ量
     * @throws IllegalArgumentException カテゴリ名が登録されていないとき。
     */
    public StrageGUI(Player player, String categoryName, int quantity) throws IllegalArgumentException {
        super(player, categoryName, Box.getInstance().getAPI().getLayouts().getStrageGUITitle().replaceAll("%category-name%", categoryName), quantity);
        
        @SuppressWarnings("serial")
        Map<Integer, ItemStack> pageCommonItems = new HashMap<Integer, ItemStack>() {{
            put(50, applyPlaceholder(layout.getStrage()));
            put(51, applyPlaceholder(layout.getShop()));
            put(52, applyPlaceholder(layout.getCraft()));
        }};
        putPageCommonItems(pageCommonItems);
        addAllItem(categories.getItems(categoryName).stream().map(layout::setStrageEntryMeta).collect(Collectors.toList()));
        setPage(getPage());
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        if (event.getSlot() == 50) {
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                Box.getInstance().getAPI().getMessages().sendMessage(getPlayer(), "gui.store-all");
                storeAll();
            }

            return;
        }
        
        if (event.getSlot() == 51) {
            event.getWhoClicked().openInventory(new ShopGUI(getPlayer(), getCategoryName(), getQuantity()).getInventory());
            return;
        }
        
        if (event.getSlot() == 52) {
            event.getWhoClicked().openInventory(new CraftGUI(getPlayer(), getCategoryName(), getQuantity()).getInventory());
            return;
        }

        if (0 <= event.getSlot() && 44 >= event.getSlot()) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) {
                super.onClicked(event);
                return;
            }
            
            if (event.isRightClick()) {
                withdraw(clickedItem);
            } else {
                deposit(clickedItem);
            }

            return;
        }

        super.onClicked(event);
    }

    @Override
    protected ItemStack applyPlaceholder(ItemStack item, Map<String, String> placeholder) {
        placeholder.put("%item-name%", Objects.requireNonNullElse(Items.getName(item, false), item.getType().toString()));
        placeholder.put("%category-name%", getCategoryName());
        placeholder.put("%stock%", String.valueOf(PlayerData.getItemAmount(getPlayer(), item)));
        return super.applyPlaceholder(item, placeholder);
    }

    /**
     * アイテムを引き出す。
     *
     * @param item 引き出すアイテム
     * @return 引き出したあとの在庫数
     */
    private long withdraw(ItemStack item) {
        Player player = getPlayer();
        ItemStack givenItem = Items.getItemStack(Items.getName(item, true));
        long stock = PlayerData.getItemAmount(player, givenItem);
        long quantity = Math.min(stock, getQuantity());
        if (stock == 0) {
            config.playNotEnoughSound(player);
            return stock;
        }
        givenItem.setAmount((int) quantity);
        int nonAdded = addItem(player.getInventory(), givenItem).values().stream()
                .mapToInt(ItemStack::getAmount).sum();
        config.playWithdrawSound(player);
        if (player.getGameMode() != GameMode.CREATIVE && !player.hasPermission("box-admin.creative")) {
            PlayerData.setItemAmount(player, givenItem, stock + nonAdded - quantity);
            update(item);
            return (long) (stock + nonAdded - quantity);
        }

        return (long) stock;
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

    /**
     * アイテムを預ける。
     *
     * @param item 預けるアイテム
     * @return 預けたあとの在庫数
     */
    private long deposit(ItemStack item) {
        Player player = getPlayer();
        ItemStack takenItem = Items.getItemStack(Items.getName(item, true));
        long stock = PlayerData.getItemAmount(player, takenItem);
        int quantity = getQuantity();
        // Items.getItemStack(Items.getName(item, true));
        takenItem = takenItem.clone();
        takenItem.setAmount(quantity);
        int nonRemoved = player.getInventory().removeItem(takenItem).values().stream().mapToInt(ItemStack::getAmount)
                .sum();
        if (nonRemoved == quantity) {
            config.playNotEnoughSound(player);
            return stock;
        }
        config.playDepositSound(player);
        if (player.getGameMode() != GameMode.CREATIVE && !player.hasPermission("box-admin.creative")) {
            PlayerData.setItemAmount(player, takenItem, stock - nonRemoved + quantity);
            update(item);
            return (long) (stock - nonRemoved + quantity);
        }

        return (long) stock;
    }

    @Override
    protected void update(ItemStack item) {
        applyPlaceholder(layout.setStrageEntryMeta(item));
    }

    /**
     * プレイヤーの手持ちのアイテムをすべてBoxに預ける。
     */
    private void storeAll() {
        boolean isModified = false;
        ItemStack[] contents = getPlayer().getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || Items.getName(item, false) == null) {
                continue;
            }
            long stock = PlayerData.getItemAmount(getPlayer(), item);
            int amount = item.getAmount();
            amount -= getPlayer().getInventory().removeItem(item).values().stream().map(ItemStack::getAmount).mapToInt(Integer::valueOf).sum();
            PlayerData.setItemAmount(getPlayer(), item, stock + amount);
            isModified = true;
        }
        if (isModified) {
            config.playDepositSound(getPlayer());
            update();
        }
    }
}