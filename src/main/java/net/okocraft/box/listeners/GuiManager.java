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

package net.okocraft.box.listeners;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.val;

import net.okocraft.box.util.MessageConfig;
import net.okocraft.box.util.OtherUtil;

import org.bukkit.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.plugin.Plugin;

import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

public class GuiManager implements Listener {
    private Database      database;
    private GeneralConfig config;
    private MessageConfig messageConfig;

    private Map<String, MemorySection> category;
    private String                     categorySelectionGuiName;
    private Map<String, String>        categoryGuiNameMap;
    private List<Integer>              categorySelectionGuiFrame;
    private NamespacedKey              categoryNameKey;
    private NamespacedKey              quantityKey;

    public GuiManager(Database database, Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.database = database;

        // config
        config        = Box.getInstance().getGeneralConfig();
        messageConfig = Box.getInstance().getMessageConfig();

        category                 = config.getCategories();
        categoryGuiNameMap       = config.getCategoryGuiNameMap();
        categorySelectionGuiName = config.getCategorySelectionGuiName();

        quantityKey     = new NamespacedKey(plugin, "quantity");
        categoryNameKey = new NamespacedKey(plugin, "categoryname");

        categorySelectionGuiFrame = List.of(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47,  48, 49, 50, 51, 52, 53
        );
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        val inventoryTitle = event.getView().getTitle();
        
        if (inventoryTitle.equals(categorySelectionGuiName)) {
            event.setCancelled(true);
            
            onClickedCategorySelectionGui(event);
            return;
        }
        
        if (categoryGuiNameMap.containsValue(inventoryTitle)) {
            event.setCancelled(true);

            onClickedCategoryGui(event);
            return;
        }
    }

    private void onClickedCategorySelectionGui(InventoryClickEvent event) {
        val player = (Player) event.getWhoClicked();
        val action = event.getAction();
        val inventory = event.getClickedInventory();

        if (inventory == null) {
            return;
        }

        int clickedSlot = event.getSlot();
        val clickedItem = inventory.getItem(clickedSlot);

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (action == InventoryAction.NOTHING || inventory.getType() == InventoryType.PLAYER) {
            return;
        }

        if (categorySelectionGuiFrame.contains(clickedSlot)) {
            return;
        }

        val categoryName = clickedItem.getItemMeta()
                // NOTE: NPE はItemStackがAIRの場合のみしか起こらない。
                .getCustomTagContainer()
                .getCustomTag(categoryNameKey, ItemTagType.STRING);

        openCategoryGui(player, categoryName);

        return;
    }

    private void onClickedCategoryGui(InventoryClickEvent event) {
        val player = (Player) event.getWhoClicked();
        val action = event.getAction();
        val inventory = event.getClickedInventory();

        if (inventory == null) {
            return;
        }

        int clickedSlot = event.getSlot();
        val clickedItem = inventory.getItem(clickedSlot);

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (action == InventoryAction.NOTHING || inventory.getType() == InventoryType.PLAYER) {
            return;
        }

        val categoryName = clickedItem
                .getItemMeta()
                // NOTE: NPE はItemStackがAIRの場合のみしか起こらない。
                .getCustomTagContainer()
                .getCustomTag(categoryNameKey, ItemTagType.STRING);

        
        val firstItem = Optional.ofNullable(inventory.getItem(0));
        int currentQuantity = firstItem.map(
                    item -> Optional.ofNullable(item.getItemMeta()).map(
                            meta -> meta.getCustomTagContainer().getCustomTag(quantityKey, ItemTagType.INTEGER)
                    ).orElse(1)
        ).orElse(1);

        // ページ移動
        if (clickedSlot == 45 || clickedSlot == 53) {
            playSound(player, config.getChangePageSound());

            openCategoryGui(
                    player,
                    categoryName,
                    Optional.ofNullable(inventory.getItem(clickedSlot)).map(ItemStack::getAmount).orElse(1),
                    inventory
            );

            changeQuantity(inventory, currentQuantity);

            return;
        }

        // カテゴリー選択GUIに戻る
        if (clickedSlot == 49) {
            playSound(player, config.getBackToGuiSound());

            openCategorySelectionGui(player);

            return;
        }

        // 取引数増減
        if (List.of(46, 47, 48, 50, 51, 52).contains(clickedSlot)) {
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
            currentQuantity = (currentQuantity + difference >= 1) ? currentQuantity + difference : 1;
            if (currentQuantity > 640) {
                currentQuantity = 640;
            }  

            // 音を鳴らす
            if (difference < 0) {
                playSound(player, config.getDecreaseSound());
            } else if (difference > 0) {
                playSound(player, config.getIncreaseSound());
            }

            changeQuantity(inventory, currentQuantity);

            return;
        }

        // 以下全て引き出し・預け入れ処理
        val clickedItemMaterial = clickedItem.getType();

        val categorySetting = category.get(categoryName);
        val clickedItemMaterialSection = GeneralConfig.getMemorySection(
                categorySetting.get("item." + clickedItemMaterial.name())
        );

        if (!clickedItemMaterialSection.isPresent()) {
            player.sendMessage(messageConfig.getErrorOccurred());
            player.closeInventory();

            return;
        }

        // アイテムの取引数
        int quantity = Optional.ofNullable(clickedItem.getItemMeta()).map(item ->
                item.getCustomTagContainer().getCustomTag(quantityKey, ItemTagType.INTEGER)
        ).orElse(1);

        // creative状態であるか、box.creativeの権限を持っていると無限に引き出せるようになる。
        if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("box.creative")) {
            if (event.isRightClick()) {
                playSound(player, config.getTakeOutSound());
                player.getInventory().addItem(new ItemStack(clickedItemMaterial, quantity));
            } else {
                playSound(player, config.getTakeOutSound());
                player.getInventory().removeItem(new ItemStack(clickedItemMaterial, quantity));
            }
            return;
        }

        // データベースに保存されているアイテム数
        val storedItemAmount = OtherUtil.parseIntOrDefault(
                database.get(clickedItemMaterial.name(), player.getUniqueId().toString()),
                Integer.MIN_VALUE
        );

        if (storedItemAmount == Integer.MIN_VALUE) {
            player.sendMessage(messageConfig.getDatabaseInvalidValue());

            return;
        }

        int resultStoredAmount;

        // 引き出し処理
        if (event.isRightClick()) {
            if (storedItemAmount <= 0) {
                playSound(player, config.getNotEnoughSound());

                return;
            }

            playSound(player, config.getTakeOutSound());

            int quantityLimited = (storedItemAmount < quantity) ? storedItemAmount : quantity;

            val nonStoredItemStacks = player.getInventory().addItem(
                    new ItemStack(clickedItemMaterial, quantityLimited)
            );

            int nonAddedAmount = nonStoredItemStacks.values().stream()
                    .mapToInt(ItemStack::getAmount).sum();

            resultStoredAmount = storedItemAmount - quantityLimited + nonAddedAmount;

        // 預け入れ処理
        } else {
            val playerInventory = event.getView().getBottomInventory();

            int playerItemAmount = playerInventory.all(clickedItemMaterial).values().stream()
                    .filter(item -> !item.hasItemMeta()).mapToInt(ItemStack::getAmount).sum();

            if (playerItemAmount == 0) {
                playSound(player, config.getNotEnoughSound());

                return;
            }

            playSound(player, config.getTakeOutSound());

            val nonRemovedItemStacks = playerInventory.removeItem(new ItemStack(clickedItemMaterial, quantity));
            int nonRemovedAmount = nonRemovedItemStacks.values().stream()
                    .mapToInt(ItemStack::getAmount).sum();

            resultStoredAmount = storedItemAmount + quantity - nonRemovedAmount;
        }

        // データベースに保存して終了
        val newValue = String.valueOf(resultStoredAmount);

        database.set(clickedItemMaterial.name(), player.getUniqueId().toString(), newValue);

        inventory.setItem(clickedSlot, createItem(
                newValue, clickedItemMaterial.name(), clickedItemMaterialSection.get(), categoryName, quantity
        ));

    }

    public void openCategorySelectionGui(Player player) {
        player.closeInventory();

        playSound(player, config.getOpenSound());

        val glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        val itemMeta  = Optional.ofNullable(glass.getItemMeta());

        itemMeta.ifPresent(meta -> {
            meta.setDisplayName("§6");
            glass.setItemMeta(meta);
        });

        val categorySelectionGui = Bukkit.createInventory(null, 54, categorySelectionGuiName);

        categorySelectionGuiFrame.forEach(slot ->
                categorySelectionGui.setItem(slot, glass.clone())
        );

        category.forEach((categoryName, categorySetting) ->
                categorySelectionGui.addItem(createCategorySelectionItem(categoryName, categorySetting))
        );

        player.openInventory(categorySelectionGui);
    }

    private ItemStack createCategorySelectionItem(String categoryName, MemorySection section) {
        val categoryIconMaterial = Material.valueOf(Optional.ofNullable(
                section.getString("icon")).orElse("BARRIER").toUpperCase()
        );
        val categorySelectionItem     = new ItemStack(categoryIconMaterial);
        val categorySelectionItemMeta = Optional.ofNullable(categorySelectionItem.getItemMeta());
        val categoryItemName          = Optional.ofNullable(section.getString("display_name"))
                .orElseThrow(() -> new NoSuchElementException("No display_name defined."))
                .replaceAll("&([a-f0-9])", "§$1");

        categorySelectionItemMeta.ifPresent(meta -> {
            meta.setDisplayName(categoryItemName);
            meta.getCustomTagContainer().setCustomTag(
                    categoryNameKey, ItemTagType.STRING, categoryName
            );

            categorySelectionItem.setItemMeta(meta);
        });

        return categorySelectionItem;
    }

    private void openCategoryGui(Player player, String categoryName) {
        openCategoryGui(player, categoryName, 1, null);
    }

    private void openCategoryGui(Player player, String categoryName, int page, Inventory modifiedInventory) {
        val categorySetting = category.get(categoryName);
        val categoryItems   = GeneralConfig.getMemorySection(categorySetting.get("item"));

        if (!categoryItems.isPresent()) {
            player.sendMessage(messageConfig.getErrorOccurredOnGUI());
            player.closeInventory();

            return;
        }

        val categoryGui = Bukkit.createInventory(player, 54, categoryGuiNameMap.get(categoryName));

        config.getFooterItemStacks().forEach((slot, itemStack) -> {
            val usingItemStack = itemStack.clone();

            Optional.ofNullable(usingItemStack.getItemMeta()).ifPresent(meta ->
                    meta.getCustomTagContainer().setCustomTag(categoryNameKey, ItemTagType.STRING, categoryName)
            );

            if (slot == 45) {
                if (page > 1) {
                    usingItemStack.setAmount(page - 1);
                } else {
                    usingItemStack.setAmount(0);
                }
            }

            if (slot == 53) {
                if (page < 64) {
                    usingItemStack.setAmount(page + 1);
                } else {
                    usingItemStack.setAmount(0);
                }
            }

            categoryGui.setItem(slot, usingItemStack);
        });

        val columnValueMap = database.getMultiValue(
                categoryItems.get().getKeys(false).stream()
                        .filter(itemName -> Material.getMaterial(itemName) != null)
                        .collect(Collectors.toList()),
                player.getUniqueId().toString()
        );

        categoryItems.get().getValues(false).entrySet().stream()
                .skip(45 * (page - 1))
                .limit(45)
                .forEach(itemEntry -> {
                    val section = GeneralConfig.getMemorySection(itemEntry.getValue());

                    if (!section.isPresent()) {
                        return;
                    }

                    if (Material.getMaterial(itemEntry.getKey()) == null) {
                        return;
                    }

                    val item = createItem(
                            columnValueMap.get(itemEntry.getKey()),
                            itemEntry.getKey(),
                            section.get(),
                            categoryName,
                            1
                    );

                    if (item.getType() == Material.AIR) {
                        return;
                    }

                    categoryGui.addItem(item);
                });

        playSound(player, config.getOpenSound());

        if (modifiedInventory != null) {
            modifiedInventory.setContents(categoryGui.getStorageContents());
        } else {
            player.closeInventory();
            player.openInventory(categoryGui);
        }
    }

    private ItemStack createItem(String stock, String materialName, MemorySection section, String categoryName,
            int quantity) {
        Material categoryItemMaterial;

        try {
            categoryItemMaterial = Material.valueOf(materialName);
        } catch (IllegalArgumentException exception) {
            return new ItemStack(Material.AIR);
        }

        val jp = section.getString("jp", "");
        val en = section.getString("en", "");
        val categoryItemName = replacePlaceholders(config.getItemTemplateName(), jp, en, stock, quantity);
        val categoryItemLore = new ArrayList<>(config.getItemTemplateLore());

        categoryItemLore.replaceAll(loreLine ->
                replacePlaceholders(loreLine, jp, en, stock, quantity)
        );

        val categoryItem     = new ItemStack(categoryItemMaterial);
        val categoryItemMeta = Optional.ofNullable(categoryItem.getItemMeta());

        categoryItemMeta.ifPresent(meta -> {
            meta.setDisplayName(categoryItemName);
            meta.setLore(categoryItemLore);

            val tagContainer = meta.getCustomTagContainer();
            tagContainer.setCustomTag(quantityKey, ItemTagType.INTEGER, quantity);
            tagContainer.setCustomTag(categoryNameKey, ItemTagType.STRING, categoryName);

            categoryItem.setItemMeta(meta);
        });

        return categoryItem;
    }

    private void changeQuantity(Inventory inv, int quantity) {
        val player = (Player) inv.getViewers().get(0);
        val firstItem = inv.getItem(0);
        if (firstItem == null || firstItem.getType() == Material.AIR) {
            return;
        }
        val categoryName = firstItem
                // NOTE: NPE はItemStackがAIRの場合のみしか起こらない。
                .getItemMeta()
                .getCustomTagContainer()
                .getCustomTag(categoryNameKey, ItemTagType.STRING);

        if (categoryName == null) {
            player.sendMessage(messageConfig.getErrorFetchCategoryName());

            return;
        }

        val section = GeneralConfig.getMemorySection(
                config.getStoringItemConfig().get("categories." + categoryName + ".item")
        );

        if (!section.isPresent()) {
            player.sendMessage(messageConfig.getErrorFetchItemConfig());

            return;
        }

        val items = IntStream.rangeClosed(0, 44).boxed()
                .map(inv::getItem)
                .filter(Objects::nonNull).collect(Collectors.toList());

        val itemMaterialNameList = (new ArrayList<>(items)).stream()
                .map(item -> item.getType().name())
                .collect(Collectors.toList());

        val itemAmountMap = database.getMultiValue(itemMaterialNameList, player.getUniqueId().toString());

        items.forEach(item -> {
            val itemMaterialName = item.getType().name();
            val itemMeta = Optional.ofNullable(item.getItemMeta());

            itemMeta.ifPresent(meta -> {
                meta.getCustomTagContainer().setCustomTag(quantityKey, ItemTagType.INTEGER, quantity);

                val jp = section.get().getString(itemMaterialName + ".jp", "");
                val en = section.get().getString(itemMaterialName + ".en", "");
                val stock = itemAmountMap.get(itemMaterialName);

                val itemName = replacePlaceholders(config.getItemTemplateName(), jp, en, stock, quantity);
                val itemLore = new ArrayList<>(config.getItemTemplateLore());

                itemLore.replaceAll(loreLine ->
                        replacePlaceholders(loreLine, jp, en, stock, quantity)
                );

                meta.setDisplayName(itemName);
                meta.setLore(itemLore);

                item.setItemMeta(meta);
            });
        });
    }

    private String replacePlaceholders(String original, String jp, String en, String stock, int quantity) {
        return original
                .replaceAll("%item_jp%", jp)
                .replaceAll("%item_en%", en)
                .replaceAll("%item_quantity%", String.valueOf(quantity))
                .replaceAll("%stock%", stock)
                .replaceAll("&([a-f0-9])", "§$1");
    }

    /**
     * プレイヤーに音声を流す。
     *
     * @param player プレイヤー
     * @param sound  流す音声
     */
    private void playSound(Player player, Sound sound) {
        player.playSound(
                player.getLocation(),
                sound,
                SoundCategory.MASTER,
                config.getSoundPitch(),
                config.getSoundVolume()
        );
    }

}