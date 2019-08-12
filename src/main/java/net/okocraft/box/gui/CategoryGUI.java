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
import java.util.stream.IntStream;

import com.google.common.primitives.Ints;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
import net.okocraft.box.database.Database;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.PlayerUtil;

class CategoryGUI implements Listener {

    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();
    private static final Database DATABASE = INSTANCE.getDatabase();

    private final Player player;
    private final ConfigurationSection categorySection;
    @Getter
    private int page;
    @Getter
    private int quantity;
    private Inventory gui;
    private final List<ItemStack> items;
    private final Map<Material, Integer> itemStockMap;
    private final List<Material> stockChangedItems;

    /**
     * コンストラクタ
     * 
     * @param player カテゴリ選択GUIでアイコンをクリックしたプレイヤー
     * @param categoryName 選択されたカテゴリの名前
     * @param quantity 引き出し・預け入れ量
     * @throws IllegalArgumentException カテゴリ名が登録されていないとき。
     */
    public CategoryGUI(Player player, String categoryName, int quantity) throws IllegalArgumentException {
        Map<String, ConfigurationSection> categories = CONFIG.getCategories();
        if (!categories.containsKey(categoryName)) {
            throw new IllegalArgumentException("Category " + categoryName + " is not registered.");
        }
        this.player = player;
        this.categorySection = CONFIG.getCategories().get(categoryName);
        this.page = 1;
        this.quantity = quantity;
        String guiName = ChatColor.translateAlternateColorCodes('&', CONFIG.getCategoryGuiNameMap().get(categoryName));
        this.gui = Bukkit.createInventory(null, 54, guiName);
        ConfigurationSection categorySection = categories.get(categoryName);
        if (categorySection.isConfigurationSection("item")) {
            List<String> keys = new ArrayList<>(categorySection.getConfigurationSection("item").getKeys(false));
            items = new ArrayList<>();
            itemStockMap = DATABASE.getMultiValue(keys, player.getName()).entrySet().stream()
                    .filter(entry -> Objects.nonNull(Material.getMaterial(entry.getKey())))
                    .filter(entry -> Objects.nonNull(Ints.tryParse(entry.getValue())))
                    .map(entry -> {
                        String key = entry.getKey().toUpperCase();
                        String value = entry.getValue();

                        int stock = Integer.parseInt(value);

                        Material material = Material.getMaterial(key);

                        // itemsに追加する ------
                        ItemStack item = new ItemStack(material);

                        // meta設定
                        ItemMeta meta = item.getItemMeta();
                        String jp = categorySection.getString("item." + key + ".jp", "");
                        String en = categorySection.getString("item." + key + ".en", "");
                        String itemName = replacePlaceholders(CONFIG.getItemTemplateName(), jp, en, stock, quantity);
                        List<String> itemLore = new ArrayList<>(CONFIG.getItemTemplateLore());
                        itemLore.replaceAll(loreLine -> replacePlaceholders(loreLine, jp, en, stock, quantity));
                        meta.setDisplayName(itemName);
                        meta.setLore(itemLore);
                        item.setItemMeta(meta);
                        items.add(item);
                        // -------

                        return Map.entry(material, stock);
                    }).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new)
                    );
        } else {
            itemStockMap = new HashMap<>();
            items = new ArrayList<>();
        }
        stockChangedItems = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, INSTANCE);
        setPage(page);
        player.openInventory(gui);
    }

    /**
     * originalのプレホルを受け取った情報で置換する。
     * 
     * @param original プレホルを含むオリジナル文字列
     * @param jp アイテムの日本語名
     * @param en アイテムの英語名
     * @param stock アイテムの在庫
     * @param quantity 一度のアイテムの取引量
     * @return 置換された文字列
     */
    private String replacePlaceholders(String original, String jp, String en, int stock, int quantity) {
        return original.replaceAll("%item_jp%", jp).replaceAll("%item_en%", en)
                .replaceAll("%item_quantity%", String.valueOf(quantity)).replaceAll("%stock%", String.valueOf(stock))
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
    private void withdraw(Material item) {
        if (!itemStockMap.containsKey(item)) {
            return;
        }
        int stock = itemStockMap.get(item);
        int tempQuantity = quantity;
        if (stock == 0) {
            PlayerUtil.playSound(player, CONFIG.getNotEnoughSound());
            return;
        }
        tempQuantity = Math.min(stock, tempQuantity);
        ItemStack givenItem = new ItemStack(item, tempQuantity);
        int nonAdded = player.getInventory().addItem(givenItem).values().stream().mapToInt(ItemStack::getAmount).sum();
        itemStockMap.put(item, stock + nonAdded - tempQuantity);
        if (!stockChangedItems.contains(item)) {
            stockChangedItems.add(item);
        }
        PlayerUtil.playSound(player, CONFIG.getTakeOutSound());
        updateLore(item);
    }

    /**
     * アイテムを預ける。
     * 
     * @param item 預けるアイテム
     */
    private void deposit(Material item) {
        if (!itemStockMap.containsKey(item)) {
            return;
        }
        int stock = itemStockMap.get(item);
        ItemStack takenItem = new ItemStack(item, quantity);
        int nonRemoved = player.getInventory().removeItem(takenItem).values().stream().mapToInt(ItemStack::getAmount).sum();
        if (nonRemoved == quantity) {
            PlayerUtil.playSound(player, CONFIG.getNotEnoughSound());
            return;
        }
        itemStockMap.put(item, stock - nonRemoved + quantity);
        if (!stockChangedItems.contains(item)) {
            stockChangedItems.add(item);
        }
        PlayerUtil.playSound(player, CONFIG.getTakeInSound());
        updateLore(item);
    }

    /**
     * アイテムの取引などで変動したloreを追随させるためのメソッド。
     * 
     * @param material loreを更新するアイテムを検索するためのタイプ
     */
    private void updateLore(Material material) {
        ItemStack item = null;
        for (int i = 0; i < 45; i++) {
            ItemStack tempItem = gui.getItem(i);
            if (tempItem != null && tempItem.getType() == material) {
                item = tempItem;
                break;
            }
        }
        if (item == null) {
            return;
        }
        updateLore(item);
    }

    /**
     * GUIの初期化や、アイテムの取引などで変動したloreを追随させるためのメソッド。
     * 
     * @param item loreを更新するアイテム
     */
    private void updateLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String materialName = item.getType().name();
        String jp = categorySection.getString("item." + materialName + ".jp", "");
        String en = categorySection.getString("item." + materialName + ".en", "");
        int stock = itemStockMap.get(item.getType());
        List<String> itemLore = new ArrayList<>(CONFIG.getItemTemplateLore());
        itemLore.replaceAll(loreLine -> replacePlaceholders(loreLine, jp, en, stock, quantity));
        meta.setLore(itemLore);
        item.setItemMeta(meta);
    }

    /**
     * 在庫の変更をデータベースに保存する。
     */
    private void commit() {
        Map<String, String> change = stockChangedItems.stream().collect(Collectors.toMap(
            Enum::name,
            item -> String.valueOf(itemStockMap.get(item)),
            (e1, e2) -> e1,
            HashMap::new
        ));
        DATABASE.setMultiValue(change, player.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer() != player) {
            return;
        }
        player.closeInventory();
    }

    @EventHandler
    public void onGuiClosed(InventoryCloseEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        commit();
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
        
        if (CONFIG.getDisabledWorlds().contains(event.getWhoClicked().getWorld())) {
            return;
        }

        int clickedSlot = event.getSlot();
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
        
        Material itemType = clickedItem.getType();
        if (!itemStockMap.containsKey(itemType)) {
            return;
        }

        // creative状態であるか、box.creativeの権限を持っていると無限に引き出せるようになる。
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("box.creative")) {
            if (event.isRightClick()) {
                PlayerUtil.playSound(player, CONFIG.getTakeOutSound());
                player.getInventory().addItem(new ItemStack(itemType, quantity));
            } else {
                PlayerUtil.playSound(player, CONFIG.getTakeOutSound());
                player.getInventory().removeItem(new ItemStack(itemType, quantity));
            }
            return;
        }

        if (event.isRightClick()) {
            withdraw(itemType);
        } else {
            deposit(itemType);
        }
    }
}