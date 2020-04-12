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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.milkbowl.vault.economy.Economy;
import net.okocraft.box.Box;
import net.okocraft.box.config.Config;
import net.okocraft.box.config.Messages;
import net.okocraft.box.config.Prices;
import net.okocraft.box.config.Categories.Category;
import net.okocraft.box.config.Config.PageFunctionItems;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.CraftRecipes;
import net.okocraft.box.util.PlayerUtil;
import org.jetbrains.annotations.Nullable;

class CategoryGUI implements Listener, InventoryHolder {

    @Nullable
    private static final Box plugin = Box.getInstance();
    private static final Prices PRICES = Prices.getInstance();

    private final Player player;
    // private final Category category;
    private int page;
    private int quantity = 1;
    private Inventory gui;
    private final List<ItemStack> items;
    private Operations operation = Operations.TRANSACTION;

    public enum Operations {
        TRANSACTION(50), BUY_AND_SALL(51), CRAFT(52);

        private int slot;

        private Operations(int slot) {
            this.slot = slot;
        }

        public int getSlot() {
            return slot;
        }

        public Operations get() {
            return this;
        }

        public static Operations get(int slot) {
            for (Operations operation : values()) {
                if (operation.getSlot() == slot) {
                    return operation;
                }
            }
            return null;
        }
    }

    /**
     * コンストラクタ
     *
     * @param player       カテゴリ選択GUIでアイコンをクリックしたプレイヤー
     * @param categoryName 選択されたカテゴリの名前
     * @param quantity     引き出し・預け入れ量
     * @throws IllegalArgumentException カテゴリ名が登録されていないとき。
     */
    public CategoryGUI(Player player, Category category, int quantity) throws IllegalArgumentException {
        this.player = player;
        // this.category = category;
        this.page = 1;
        this.quantity = Math.min(quantity, Config.getConfig().getMaxQuantity());
        this.gui = Bukkit.createInventory(this, 54, category.getDisplayName());
        this.items = new ArrayList<>() {
            private static final long serialVersionUID = 1L;

            {
                category.getItems().forEach(item -> {
                    ItemStack itemStack = Items.getItemStack(item);
                    updateLore(itemStack);
                    add(itemStack);
                });
            }
        };

        Bukkit.getPluginManager().registerEvents(this, plugin);
        setPage(page);
        player.openInventory(gui);
    }

    /**
     * originalのプレホルを受け取った情報で置換する。
     *
     * @param original プレホルを含むオリジナル文字列
     * @param item     プレホルの情報を割り出すアイテム
     * @return 置換された文字列
     */
    private String replacePlaceholders(String original, ItemStack item) {
        Economy economy = plugin.getEconomy();
        if (economy == null) {
            return original;
        }
        return original.replaceAll("%balance%", String.valueOf(economy.getBalance(player)))
                .replaceAll("%item-quantity%", Integer.toString(quantity))
                .replaceAll("%stock%", String.valueOf(PlayerData.getItemAmount(player, item)))
                .replaceAll("%buy-price%", String.valueOf(PRICES.getBuyPrice(item)))
                .replaceAll("%sell-price%", String.valueOf(PRICES.getSellPrice(item)))
                .replaceAll("%amount%", String.valueOf(quantity * CraftRecipes.getResultAmount(item)))
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
        List<ItemStack> items = new ArrayList<>(this.items);
        if (operation == Operations.BUY_AND_SALL) {
            items.removeIf(item -> PRICES.getBuyPrice(item) == 0 && PRICES.getSellPrice(item) == 0);
            items.removeIf(item -> PlayerData.getItemAmount(player, item) == 0
                    && PRICES.getBuyPrice(item) > plugin.getEconomy().getBalance(player));
        } else if (operation == Operations.CRAFT) {
            items = CraftRecipes.filterUnavailable(player, items, quantity);
        }
        int maxPage = (items.size() % 45 == 0) ? items.size() / 45 : items.size() / 45 + 1;
        page = Math.min(page, maxPage);
        page = Math.max(page, 1);
        this.page = page;
        gui.clear();
        List<ItemStack> functionItems = Arrays.stream(Config.PageFunctionItems.values()).map(PageFunctionItems::getItem)
                .collect(Collectors.toList());
        for (int i = 0; i < functionItems.size(); i++) {
            if (i == 0) {
                functionItems.get(i).setAmount(page - 1);
            } else if (i == 8) {
                if (page == maxPage) {
                    functionItems.get(i).setAmount(0);
                } else {
                    functionItems.get(i).setAmount(page == 64 ? 0 : page + 1);
                }
            }
            gui.setItem(i + 45, functionItems.get(i));
        }
        gui.getItem(operation.getSlot()).addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        gui.addItem(items.stream().skip(45 * (page - 1)).limit(45).toArray(ItemStack[]::new));
        updateLores();
        PlayerUtil.playSound(player, Config.Sounds.CHANGE_PAGE);
    }

    /**
     * 取引量を変える。
     *
     * @param newQuantity 新しい取引量
     */
    private void setQuantity(int newQuantity) {
        newQuantity = Math.min(newQuantity, Config.getConfig().getMaxQuantity());
        if (quantity < newQuantity) {
            PlayerUtil.playSound(player, Config.Sounds.INCREASE_UNIT);
        } else if (quantity > newQuantity) {
            PlayerUtil.playSound(player, Config.Sounds.DECREASE_UNIT);
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
     * @return 引き出したあとの在庫数
     */
    private long withdraw(ItemStack item) {
        long stock = PlayerData.getItemAmount(player, item);
        long tempQuantity = quantity;
        if (stock == 0) {
            PlayerUtil.playSound(player, Config.Sounds.NOT_ENOUGH);
            return stock;
        }
        tempQuantity = Math.min(stock, tempQuantity);
        ItemStack givenItem = Items.getItemStack(Items.getName(item, true));
        givenItem.setAmount((int) tempQuantity);
        int nonAdded = addItem(player.getInventory(), splitStack(givenItem)).values().stream().mapToInt(ItemStack::getAmount).sum();
        PlayerData.setItemAmount(player, item, stock + nonAdded - tempQuantity);
        PlayerUtil.playSound(player, Config.Sounds.WITHDRAW);
        updateLore(item);
        return (long) (stock + nonAdded - tempQuantity);
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

    private ItemStack[] splitStack(ItemStack item) {
        item = item.clone();
        int amount = item.getAmount();
        int maxSize = item.getMaxStackSize();

        List<ItemStack> resultList = new ArrayList<>();
        while(amount - maxSize > 0) {
            ItemStack split = item.clone();
            split.setAmount(maxSize);
            resultList.add(split);
            amount -= maxSize;
        }

        if (amount > 0) {
            item.setAmount(amount);
            resultList.add(item);
            amount = 0;
        }

        return resultList.toArray(ItemStack[]::new);
    }

    /**
     * アイテムを預ける。
     *
     * @param item 預けるアイテム
     * @return 預けたあとの在庫数
     */
    private long deposit(ItemStack item) {
        long stock = PlayerData.getItemAmount(player, item);
        ItemStack takenItem = Items.getItemStack(Items.getName(item, true));
        takenItem.setAmount(quantity);
        int nonRemoved = player.getInventory().removeItem(takenItem).values().stream().mapToInt(ItemStack::getAmount)
                .sum();
        if (nonRemoved == quantity) {
            PlayerUtil.playSound(player, Config.Sounds.NOT_ENOUGH);
            return stock;
        }
        PlayerData.setItemAmount(player, item, stock - nonRemoved + quantity);
        PlayerUtil.playSound(player, Config.Sounds.DEPOSIT);
        updateLore(item);
        return (long) (stock - nonRemoved + quantity);
    }

    /**
     * アイテムを売る
     * 
     * @param item 売るアイテム。
     * @return 売ったアイテム数
     */
    private long sell(ItemStack item) {
        double price = PRICES.getSellPrice(item);
        long stock = PlayerData.getItemAmount(player, item);
        long tempQuantity = quantity;

        if (stock == 0 || price == 0) {
            PlayerUtil.playSound(player, Config.Sounds.NOT_ENOUGH);
            return 0;
        } else if (stock < tempQuantity) {
            tempQuantity = stock;
        }

        Economy economy = plugin.getEconomy();
        economy.depositPlayer(player, tempQuantity * price);
        PlayerData.setItemAmount(player, item, stock - tempQuantity);
        PlayerUtil.playSound(player, Config.Sounds.SELL);
        updateLore(item);
        return tempQuantity;
    }

    /**
     * アイテムを売る
     * 
     * @param item 売るアイテム。
     * @return 買って得たアイテム数
     */
    private long buy(ItemStack item) {
        double price = PRICES.getBuyPrice(item);
        Economy economy = plugin.getEconomy();
        double balance = economy.getBalance(player);
        long stock = PlayerData.getItemAmount(player, item);

        int tempQuantity = quantity;
        if (balance == 0 || price == 0) {
            PlayerUtil.playSound(player, Config.Sounds.NOT_ENOUGH);
            return 0;
        } else if (price * quantity > balance) {
            tempQuantity = (int) (balance / price);
        }

        economy.withdrawPlayer(player, tempQuantity * price);
        PlayerData.setItemAmount(player, item, stock + tempQuantity);
        PlayerUtil.playSound(player, Config.Sounds.SELL);
        updateLore(item);
        return tempQuantity;
    }

    private long craft(ItemStack item) {
        Map<String, Integer> stacked = CraftRecipes.getIngredient(item);
        long tempQuantity = quantity;
        for (Map.Entry<String, Integer> entry : stacked.entrySet()) {
            // 材料の在庫から、作れるアイテム数を割り出す
            long materialStock = PlayerData.getItemAmount(player, Items.getItemStack(entry.getKey()));
            tempQuantity = Math.min(entry.getValue() * tempQuantity, materialStock) / entry.getValue();
        }
        if (tempQuantity == 0) {
            PlayerUtil.playSound(player, Config.Sounds.NOT_ENOUGH);
            return 0;
        }
        for (Map.Entry<String, Integer> entry : stacked.entrySet()) {
            ItemStack ingredient = Items.getItemStack(entry.getKey());
            long stock = PlayerData.getItemAmount(player, ingredient);
            PlayerData.setItemAmount(player, ingredient, stock - tempQuantity * entry.getValue());
        }
        long stock = PlayerData.getItemAmount(player, item);
        long add = tempQuantity * CraftRecipes.getResultAmount(item);
        PlayerData.setItemAmount(player, item, stock + add);
        updateLore(item);
        return add;
    }

    private void storeAll() {
        boolean isModified = false;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || Items.getName(item, false) == null) {
                continue;
            }
            long stock = PlayerData.getItemAmount(player, item);
            int amount = item.getAmount();
            amount -= player.getInventory().removeItem(item).values().stream().map(ItemStack::getAmount).mapToInt(Integer::valueOf).sum();
            PlayerData.setItemAmount(player, item, stock + amount);
            isModified = true;
        }
        if (isModified) {
            PlayerUtil.playSound(player, Config.Sounds.DEPOSIT);
            updateLores();
        }
    }

    private double sellAll() {
        double sum = 0D;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || Items.getName(item, false) == null) {
                continue;
            }
            int amount = item.getAmount();
            amount -= player.getInventory().removeItem(item).values().stream().map(ItemStack::getAmount).mapToInt(Integer::valueOf).sum();
            double price = PRICES.getSellPrice(item) * amount;
            sum += price;
        }
        if (sum > 0) {
            plugin.getEconomy().depositPlayer(player, sum);
            PlayerUtil.playSound(player, Config.Sounds.SELL);
            if (operation == Operations.BUY_AND_SALL) {
                updateLores();
            }
        }
        return sum;
    }

    private void select(Operations operation) {
        if (operation == null || this.operation == operation) {
            return;
        }
        this.operation = operation;
        setPage(page);
        return;
    }

    /**
     * GUIの初期化や、アイテムの取引などで変動したloreを追随させるためのメソッド。
     *
     * @param item loreを更新するアイテム
     */
    private void updateLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> itemLore;
        switch (operation) {
        case BUY_AND_SALL:
            itemLore = Config.getBuyAndSellGuiConfig().getItemLoreFormat();
            break;
        case CRAFT:
            itemLore = Config.getCraftGuiConfig().getItemLoreFormat();
            break;
        default:
            itemLore = Config.getTransactionGuiConfig().getItemLoreFormat();
            break;
        }
        itemLore.replaceAll(loreLine -> replacePlaceholders(loreLine, item));
        if (operation == Operations.CRAFT) {
            List<String> ingredientsLore = CraftRecipes.getIngredient(item).entrySet().stream()
                    .map(entry -> Config.getCraftGuiConfig().getItemRecipeLineFormat().replaceAll("%material%", entry.getKey())
                            .replaceAll("%material-stock%",
                                    String.valueOf(
                                            PlayerData.getItemAmount(player, Items.getItemStack(entry.getKey()))))
                            .replaceAll("%amount%", String.valueOf(entry.getValue() * quantity))
                            .replaceAll("&([a-f0-9])", "§$1"))
                    .collect(Collectors.toList());
            for (int i = itemLore.size() - 1; i >= 0; i--) {
                if (itemLore.get(i).contains("%materials%")) {
                    itemLore.remove(i);
                    itemLore.addAll(i, ingredientsLore);
                }
            }
        }
        meta.setLore(itemLore);
        item.setItemMeta(meta);
    }

    @Override
    public Inventory getInventory() {
        return gui;
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

        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onClicked(InventoryClickEvent event) {
        if (player != event.getWhoClicked()) {
            return;
        }

        if (event.getInventory().getHolder() != this) {
            return;
        }

        InventoryAction action = event.getAction();
        event.setCancelled(true);

        if (Config.getConfig().getDisabledWorlds().contains(event.getWhoClicked().getWorld())) {
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
            PlayerUtil.playSound(player, Config.Sounds.MENU_BACK);
            player.closeInventory();
            player.openInventory(CategorySelectorGUI.GUI);
            return;
        }

        // 取引数変更単位を調節
        if (clickedSlot == 47) {
            ItemStack changeUnitItem = gui.getItem(47);
            if (changeUnitItem == null) {
                return;
            }
            double multiplier = event.isRightClick() ? 0.5 : 2;
            int result = Math.max((int) (gui.getItem(46).getAmount() * multiplier), 1);
            result = Math.min(result, 64);
            gui.getItem(46).setAmount(result);
            gui.getItem(48).setAmount(result);
            return;
        }

        // 取引数増減
        if (clickedSlot == 46 || clickedSlot == 48) {

            int currentQuantity = quantity;
            int difference = (clickedSlot == 46 ? -1 : 1) * gui.getItem(46).getAmount();

            // 既に取引数が上限または下限に達している場合
            if ((currentQuantity == Config.getConfig().getMaxQuantity() && difference > 0)
                    || (currentQuantity == 1 && difference < 0)) {
                return;
            }

            // 取引数が上限または下限をに達した場合に制限をかける処理
            currentQuantity = Math.max(currentQuantity + difference, 1);
            currentQuantity = Math.min(currentQuantity, Config.getConfig().getMaxQuantity());

            setQuantity(currentQuantity);
            return;
        }

        if (clickedSlot == 50) {
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                Messages.getInstance().sendMessage(player, "gui.store-all");
                storeAll();
            } else if (event.isLeftClick()) {
                select(Operations.TRANSACTION);
            }
            return;
        } else if (clickedSlot == 51) {
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                double money = sellAll();
                if (money > 0) {
                    Messages.getInstance().sendMessage(player, "gui.sell-all", Map.of("%money%", money));
                }
            } else if (event.isLeftClick()) {
                select(Operations.BUY_AND_SALL);
            }
            return;
        } else if (clickedSlot == 52 && event.isLeftClick()) {
            select(Operations.CRAFT);
            return;
        }

        // creative状態であるか、box.creativeの権限を持っていると無限に引き出せるようになる。
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("box.creative")) {
            ItemStack item = Items.getItemStack(Items.getName(clickedItem, true));
            item.setAmount(quantity);
            if (event.isRightClick()) {
                PlayerUtil.playSound(player, Config.Sounds.WITHDRAW);
                addItem(player.getInventory(), item);
            } else {
                PlayerUtil.playSound(player, Config.Sounds.DEPOSIT);
                player.getInventory().removeItem(item);
            }
            return;
        }

        if (event.isRightClick()) {
            if (operation == Operations.BUY_AND_SALL) {
                sell(clickedItem);
            } else if (operation == Operations.TRANSACTION) {
                withdraw(clickedItem);
            }
        } else {
            if (operation == Operations.BUY_AND_SALL) {
                buy(clickedItem);
            } else if (operation == Operations.CRAFT) {
                craft(clickedItem);
            } else {
                deposit(clickedItem);
            }
        }
    }
}