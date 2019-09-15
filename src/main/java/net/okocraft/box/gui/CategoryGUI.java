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
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.Getter;
import net.okocraft.box.Box;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CategoryGUI implements Listener {

    @Nullable
    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();

    @NotNull
    private final Player player;
    private final Category category;
    @Getter
    private int page;
    @Getter
    private int quantity;
    private Inventory gui;
    @NotNull
    private final List<ItemStack> items;

    /**
     * コンストラクタ
     * 
     * @param player       カテゴリ選択GUIでアイコンをクリックしたプレイヤー
     * @param categoryName 選択されたカテゴリの名前
     * @param quantity     引き出し・預け入れ量
     * @throws IllegalArgumentException カテゴリ名が登録されていないとき。
     */
    public CategoryGUI(@NotNull Player player, String categoryName, int quantity) throws IllegalArgumentException {
        Map<String, Category> categories = CONFIG.getCategories();
        if (!categories.containsKey(categoryName)) {
            throw new IllegalArgumentException("Category " + categoryName + " is not registered.");
        }
        this.player = player;
        this.category = categories.get(categoryName);
        this.page = 1;
        this.quantity = quantity;
        this.gui = Bukkit.createInventory(null, 54, category.getDisplayName());
        this.items = new ArrayList<>() {
            private static final long serialVersionUID = 1L;
            {
                category.getItems().forEach(item -> {
                    ItemStack itemStack = item.clone();
                    // meta設定
                    ItemMeta meta = itemStack.getItemMeta();
                    long stock = PlayerData.getItemAmount(player, item);
                    List<String> itemLore = new ArrayList<>(CONFIG.getItemTemplateLore());
                    itemLore.replaceAll(loreLine -> replacePlaceholders(loreLine, stock, quantity));
                    meta.setLore(itemLore);
                    itemStack.setItemMeta(meta);
                    add(itemStack);
                });
            }
        };

        Bukkit.getPluginManager().registerEvents(this, INSTANCE);
        setPage(page);
        player.openInventory(gui);
    }

    /**
     * originalのプレホルを受け取った情報で置換する。
     * 
     * @param original プレホルを含むオリジナル文字列
     * @param jp       アイテムの日本語名
     * @param en       アイテムの英語名
     * @param stock    アイテムの在庫
     * @param quantity 一度のアイテムの取引量
     * @return 置換された文字列
     */
    @NotNull
    private String replacePlaceholders(@NotNull String original, long stock, int quantity) {
        return original.replaceAll("%item_quantity%", Integer.toString(quantity))
                .replaceAll("%stock%", String.valueOf(stock))
                .replaceAll("&([a-f0-9])", "§$1");
    }

    /**
     * 今開いているguiのページを指定したページに移動させる。
     * 
     * @param page 目的のページ
     */
    private void setPage(int page) {
        if (page <= 0 || page > 64) {
            return;
        }
        int maxPage = (items.size() % 45 == 0) ? items.size() / 45 : items.size() / 45 + 1;
        page = Math.min(page, maxPage);
        this.page = page;
        gui.clear();
        Map<Integer, ItemStack> footers = CONFIG.getFooterItemStacks();
        footers.get(45).setAmount(page - 1);
        if (page == maxPage) {
            footers.get(53).setAmount(0);
        } else {
            footers.get(53).setAmount(page == 64 ? 0 : page + 1);
        }
        IntStream.range(45, 54).boxed().forEach(slot -> gui.setItem(slot, footers.get(slot)));
        gui.addItem(items.stream().skip(45 * (page - 1)).limit(45).toArray(ItemStack[]::new));
        updateLores();
        PlayerUtil.playSound(player, CONFIG.getChangePageSound());
    }

    /**
     * 取引量を変える。
     * 
     * @param newQuantity 新しい取引量
     */
    private void setQuantity(int newQuantity) {
        if (quantity < newQuantity) {
            PlayerUtil.playSound(player, CONFIG.getIncreaseSound());
        } else if (quantity > newQuantity) {
            PlayerUtil.playSound(player, CONFIG.getDecreaseSound());
        } else {
            return;
        }
        quantity = newQuantity;
        updateLores();
    }

    /**
     * 全てのアイテムのloreを更新する。
     */
    private void updateLores() {
        for (int i = 0; i < 45; i++) {
            ItemStack item = gui.getItem(i);
            if (item != null) {
                updateLore(item);
            }
        }
    }

    /**
     * アイテムを引き出す。
     * 
     * @param item 引き出すアイテム
     */
    private void withdraw(@NotNull ItemStack item) {
        long stock = PlayerData.getItemAmount(player, item);
        long tempQuantity = quantity;
        if (stock == 0) {
            PlayerUtil.playSound(player, CONFIG.getNotEnoughSound());
            return;
        }
        tempQuantity = Math.min(stock, tempQuantity);
        ItemStack givenItem = Items.getItemStack(Items.getName(item, true));
        givenItem.setAmount((int) tempQuantity);
        int nonAdded = player.getInventory().addItem(givenItem).values().stream().mapToInt(ItemStack::getAmount).sum();
        PlayerData.setItemAmount(player, item, stock + nonAdded - tempQuantity);
        PlayerUtil.playSound(player, CONFIG.getTakeOutSound());
        updateLore(item);
    }

    /**
     * アイテムを預ける。
     * 
     * @param item 預けるアイテム
     */
    private void deposit(@NotNull ItemStack item) {
        long stock = PlayerData.getItemAmount(player, item);
        ItemStack takenItem = Items.getItemStack(Items.getName(item, true));
        takenItem.setAmount(quantity);
        int nonRemoved = player.getInventory().removeItem(takenItem).values().stream().mapToInt(ItemStack::getAmount)
                .sum();
        if (nonRemoved == quantity) {
            PlayerUtil.playSound(player, CONFIG.getNotEnoughSound());
            return;
        }
        PlayerData.setItemAmount(player, item, stock - nonRemoved + quantity);
        PlayerUtil.playSound(player, CONFIG.getTakeInSound());
        updateLore(item);
    }

    /**
     * GUIの初期化や、アイテムの取引などで変動したloreを追随させるためのメソッド。
     * 
     * @param item loreを更新するアイテム
     */
    private void updateLore(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        long stock = PlayerData.getItemAmount(player, item);
        List<String> itemLore = new ArrayList<>(CONFIG.getItemTemplateLore());
        itemLore.replaceAll(loreLine -> replacePlaceholders(loreLine, stock, quantity));
        meta.setLore(itemLore);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        if (event.getPlayer() != player) {
            return;
        }
        player.closeInventory();
    }

    @EventHandler
    public void onGuiClosed(@NotNull InventoryCloseEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onClicked(InventoryClickEvent event) {
        if (player != event.getWhoClicked()) {
            return;
        }

        Inventory inv = event.getInventory();

        if (inv == null || !gui.getItem(0).isSimilar(inv.getItem(0))) {
            return;
        }

        InventoryAction action = event.getAction();
        event.setCancelled(true);

        if (CONFIG.getDisabledWorlds().contains(event.getWhoClicked().getWorld().getName())) {
            return;
        }

        int clickedSlot = event.getSlot();
        if (clickedSlot == -999) {
            return;
        }
        ItemStack clickedItem = gui.getItem(clickedSlot);

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (action == InventoryAction.NOTHING || gui.getType() == InventoryType.PLAYER) {
            return;
        }

        // ページ移動
        if (clickedSlot == 45) {
            setPage(page - 1);
            return;
        } else if (clickedSlot == 53) {
            setPage(page + 1);
            return;
        }

        // カテゴリー選択GUIに戻る
        if (clickedSlot == 49) {
            PlayerUtil.playSound(player, CONFIG.getBackToGuiSound());
            player.closeInventory();
            player.openInventory(CategorySelectorGUI.GUI);
            return;
        }

        // 取引数増減
        if (List.of(46, 47, 48, 50, 51, 52).contains(clickedSlot)) {
            int currentQuantity = quantity;
            int difference = 0;

            switch (clickedSlot) {
            case 46:
                difference = -64;
                break;
            case 47:
                difference = -8;
                break;
            case 48:
                difference = -1;
                break;
            case 50:
                difference = 1;
                break;
            case 51:
                difference = 8;
                break;
            case 52:
                difference = 64;
                break;
            }

            // 既に取引数が上限または下限に達している場合
            if ((currentQuantity == 640 && difference > 0) || (currentQuantity == 1 && difference < 0)) {
                return;
            }

            // 取引数が上限または下限をに達した場合に制限をかける処理
            currentQuantity = Math.max(currentQuantity + difference, 1);
            currentQuantity = Math.min(currentQuantity, 640);

            setQuantity(currentQuantity);
            return;
        }

        // creative状態であるか、box.creativeの権限を持っていると無限に引き出せるようになる。
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("box.creative")) {
            ItemStack item = Items.getItemStack(Items.getName(clickedItem, true));
            item.setAmount(quantity);
            if (event.isRightClick()) {
                PlayerUtil.playSound(player, CONFIG.getTakeOutSound());
                player.getInventory().addItem(item);
            } else {
                PlayerUtil.playSound(player, CONFIG.getTakeOutSound());
                player.getInventory().removeItem(item);
            }
            return;
        }

        if (event.isRightClick()) {
            withdraw(clickedItem);
        } else {
            deposit(clickedItem);
        }
    }
}