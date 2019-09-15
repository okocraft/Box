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

package net.okocraft.box.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.okocraft.box.Box;
import net.okocraft.box.database.Items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.okocraft.box.gui.Category;
import net.okocraft.box.gui.CategorySelectorGUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author LazyGon
 */
public class GeneralConfig {
    @org.jetbrains.annotations.Nullable
    private final Box plugin;

    @NotNull
    @Getter
    private final CustomConfig itemCustomConfig;

    // FileConfiguration
    @Getter
    private FileConfiguration itemConfig;

    // Sounds
    @Getter
    private float soundPitch = 0.0F;
    @Getter
    private float soundVolume = 0.0F;
    @Getter
    private Sound openSound;
    @Getter
    private Sound takeInSound;
    @Getter
    private Sound takeOutSound;
    @Getter
    private Sound notEnoughSound;
    @Getter
    private Sound decreaseSound;
    @Getter
    private Sound increaseSound;
    @Getter
    private Sound backToGuiSound;
    @Getter
    private Sound changePageSound;

    // autoStoreSetting
    @Getter
    private boolean autoStoreEnabled;
    @Getter
    private boolean autoStoreEnabledByDefault;

    // Box Stick Item setting
    @Getter
    private String boxStickDisplayName;
    @Getter
    private List<String> boxStickLore;
    @Getter
    private boolean boxStickEnabledBlockPlace;
    @Getter
    private boolean boxStickEnabledFood;
    @Getter
    private boolean boxStickEnabledPotion;
    @Getter
    private boolean boxStickEnabledTool;

    @Getter
    private List<String> disabledWorlds;

    @Getter
    private List<World> replantWorlds;

    // Item Template
    @Getter
    private List<String> itemTemplateLore;

    // Item categories
    @Getter
    private Map<String, Category> categories;
    @Getter
    private Set<String> allItems;
    @Getter
    private Map<String, Double> sellPrice;

    // CategorySelectionGui Name
    @Getter
    private String categorySelectionGuiName;

    // CategoryGui
    @Getter
    private String categoryGuiName;
    @Getter
    private Map<Integer, ItemStack> footerItemStacks;

    public GeneralConfig() {
        this.plugin = Box.getInstance();

        itemCustomConfig = new CustomConfig(plugin, "items.yml");

        // Create some files
        plugin.saveDefaultConfig();
        itemCustomConfig.saveDefaultConfig();

        itemConfig = itemCustomConfig.getConfig();

        // Bind variables
        initConfig();
    }

    /**
     * すべての設定を再読込する。
     */
    public void reload() {
        itemCustomConfig.initConfig();

        itemConfig = itemCustomConfig.getConfig();

        initConfig();

        plugin.registerEvents();
    }

    /**
     * 設定をファイルから読み込んで初期化する。
     */
    private void initConfig() {
        // SoundSetting
        initSoundConfig();

        // AutoStore
        initAutoStoreConfig();

        // DisabledWorld
        initDisabledWorldConfig();

        // DisabledWorld
        initReplantWorldConfig();

        // Footer
        initFooterConfig();

        itemTemplateLore = itemConfig.getStringList("ItemTemplate.lore").stream()
                .map(line -> line.replaceAll("&([a-f0-9])", "§$1")).collect(Collectors.toList());

        categoryGuiName = ChatColor.translateAlternateColorCodes('&',
                Optional.ofNullable(itemConfig.getString("CategoryGui.GuiName"))
                        .orElseGet(() -> "%category_item_display_name% (%category%)"));

        categorySelectionGuiName = ChatColor.translateAlternateColorCodes('&', Optional
                .ofNullable(itemConfig.getString("CategorySelectionGui.GuiName")).orElseGet(() -> "Box - カテゴリー選択"));

        // コンフィグに書かれた順番で表示するためにLinkedHashMapを使っている。
        allItems = new HashSet<>();
        categories = new LinkedHashMap<>();
        Optional.ofNullable(itemConfig.getConfigurationSection("categories")).ifPresent(categoriesSection -> {
            categoriesSection.getKeys(false).forEach(sectionName -> {
                Optional.ofNullable(categoriesSection.getConfigurationSection(sectionName))
                        .ifPresent(categorySection -> {
                            List<String> items = categorySection.getStringList("item");
                            if (items.isEmpty()) {
                                return;
                            }
                            String displayName = categoryGuiName
                                    .replaceAll("%category_item_display_name%",
                                            categorySection.getString("display_name"))
                                    .replaceAll("%category%", sectionName);
                            String iconName = categorySection.getString("icon");
                            ItemStack icon = Items.getItemStack(iconName.toUpperCase());
                            if (icon == null) {
                                plugin.getLogger().warning("The icon name " + iconName + " is invalid.");
                                return;
                            }
                            allItems.addAll(items);
                            categories.put(sectionName, new Category(sectionName, displayName, icon, items));
                        });
            });
        });

        allItems.removeIf(item -> !Items.contains(item));

        CustomConfig priceConfig = new CustomConfig(plugin, "sellprice.yml");
        priceConfig.saveDefaultConfig();
        sellPrice = allItems.stream().collect(Collectors.toMap(Function.identity(),
                itemName -> priceConfig.getConfig().getDouble(itemName), (i1, i2) -> i1, HashMap::new));

        FileConfiguration defaultConfig = plugin.getConfig();
        boxStickDisplayName = ChatColor.translateAlternateColorCodes('&',
                defaultConfig.getString("General.BoxStick.DisplayName", "&9Box Stick"));
        boxStickLore = defaultConfig.getStringList("General.BoxStick.Lore");
        if (boxStickLore.isEmpty()) {
            boxStickLore.add("§r");
            boxStickLore.add("§7利き手じゃない手にこれを持つと、利き手の");
            boxStickLore.add("§7アイテムを使った時にBoxから消費します。");
        }
        boxStickLore.replaceAll(loreLine -> ChatColor.translateAlternateColorCodes('&', loreLine));
        
        boxStickEnabledBlockPlace = defaultConfig.getBoolean("General.BoxStick.Enabled.BlockPlace");
        boxStickEnabledFood = defaultConfig.getBoolean("General.BoxStick.Enabled.Food");
        boxStickEnabledPotion = defaultConfig.getBoolean("General.BoxStick.Enabled.Potion");
        boxStickEnabledTool = defaultConfig.getBoolean("General.BoxStick.Enabled.Tool");
    }

    /**
     * コマンドによって増えた増分のカテゴリを読み込む。
     */
    public void addCategory() {
        itemCustomConfig.initConfig();
        itemConfig = itemCustomConfig.getConfig();

        Optional.ofNullable(itemConfig.getConfigurationSection("categories")).ifPresent(categoriesSection -> {
            categoriesSection.getKeys(false).stream().filter(sectionName -> !categories.containsKey(sectionName))
                    .filter(sectionName -> itemConfig.isConfigurationSection("categories." + sectionName))
                    .map(sectionName -> itemConfig.getConfigurationSection("categories." + sectionName))
                    .forEach(categorySection -> {
                        List<String> items = categorySection.getStringList("item");
                        if (items.isEmpty()) {
                            return;
                        }
                        String name = categorySection.getName();
                        String displayName = categoryGuiName
                                .replaceAll("%category_item_display_name%", categorySection.getString("display_name"))
                                .replaceAll("%category%", name);
                        ItemStack icon = Items.getItemStack(categorySection.getString("icon").toUpperCase());
                        categories.put(name, new Category(name, displayName, icon, items));
                    });

        });

        allItems.removeIf(item -> !Items.contains(item));

        CategorySelectorGUI.restartListener();
    }

    /**
     * 音設定初期化
     *
     * @author akaregi
     * @since v1.1.0
     *
     * @see GeneralConfig#initConfig()
     */
    private void initSoundConfig() {
        FileConfiguration defaultConfig = plugin.getConfig();

        float DEFAULT_SOUND_VOLUME = 1.0F;
        float DEFAULT_SOUND_PITCH = 1.0F;

        Sound DEFAULT_OPEN_SOUND = Sound.BLOCK_CHEST_OPEN;
        Sound DEFAULT_TAKE_IN_SOUND = Sound.ENTITY_ITEM_PICKUP;
        Sound DEFAULT_TAKE_OUT_SOUND = Sound.BLOCK_STONE_BUTTON_CLICK_ON;
        Sound DEFAULT_NOT_ENOUGH_SOUND = Sound.ENTITY_ENDERMAN_TELEPORT;
        Sound DEFAULT_INCREASE_SOUND = Sound.BLOCK_TRIPWIRE_CLICK_ON;
        Sound DEFAULT_DECREASE_SOUND = Sound.BLOCK_TRIPWIRE_CLICK_OFF;
        Sound DEFAULT_CHANGE_PAGE_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        Sound DEFAULT_RETURN_TO_SELECTION_GUI_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

        soundVolume = (float) defaultConfig.getDouble("General.SoundSetting.Volume", DEFAULT_SOUND_VOLUME);
        soundPitch = (float) defaultConfig.getDouble("General.SoundSetting.Pitch", DEFAULT_SOUND_PITCH);

        openSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.Open")).orElse(DEFAULT_OPEN_SOUND);

        takeInSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.TakeIn"))
                .orElse(DEFAULT_TAKE_IN_SOUND);

        takeOutSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.TakeOut"))
                .orElse(DEFAULT_TAKE_OUT_SOUND);

        notEnoughSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.NotEnough"))
                .orElse(DEFAULT_NOT_ENOUGH_SOUND);

        increaseSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.Increase"))
                .orElse(DEFAULT_INCREASE_SOUND);

        decreaseSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.Decrease"))
                .orElse(DEFAULT_DECREASE_SOUND);

        changePageSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.ChangePage"))
                .orElse(DEFAULT_CHANGE_PAGE_SOUND);

        backToGuiSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.ReturnToSelectionGui"))
                .orElse(DEFAULT_RETURN_TO_SELECTION_GUI_SOUND);
    }

    /**
     * AutoStore 設定初期化
     *
     * @author akaregi
     * @since v1.1.0
     *
     * @see GeneralConfig#initConfig()
     */
    private void initAutoStoreConfig() {
        autoStoreEnabled = plugin.getConfig().getBoolean("General.AutoStore.Enabled", false);
        autoStoreEnabledByDefault = plugin.getConfig().getBoolean("General.AutoStore.PlayerDefault", false);
    }

    /**
     * DisabledWorld 設定初期化
     *
     * @author akaregi
     * @since v1.1.0
     *
     * @see GeneralConfig#initConfig()
     */
    private void initDisabledWorldConfig() {
        disabledWorlds = plugin.getConfig().getStringList("General.DisabledWorld");
    }

    /**
     * ReplantWorld 設定初期化
     *
     * @author LazyGon
     * @since v1.1.0
     *
     * @see GeneralConfig#initConfig()
     */
    private void initReplantWorldConfig() {
        replantWorlds = plugin.getConfig().getStringList("General.ReplantWorld").stream().map(Bukkit::getWorld)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * フッター初期化
     * 
     * @author akaregi
     * @since v1.1.0
     *
     * @see GeneralConfig#initConfig()
     */
    private void initFooterConfig() {
        // ページ送り
        String prevPage = itemConfig.getString("CategoryGui.PreviousPage", "&6前のページ &8| &6Prev Page");
        String nextPage = itemConfig.getString("CategoryGui.NextPage", "&6次のページ &8| &6Nex Page");

        // 取扱単位（減算）
        String decrease1 = itemConfig.getString("CategoryGui.Decrease1", "&7単位: &c-1");
        String decrease8 = itemConfig.getString("CategoryGui.Decrease8", "&7単位: &c-8");
        String decrease64 = itemConfig.getString("CategoryGui.Decrease64", "&7単位: &c-64");

        // 取扱単位（加算）
        String increase1 = itemConfig.getString("CategoryGui.Increase1", "&7単位: &b+1");
        String increase8 = itemConfig.getString("CategoryGui.Increase8", "&7単位: &b+8");
        String increase64 = itemConfig.getString("CategoryGui.Increase64", "&7単位: &b+64");

        // 戻る
        String back = itemConfig.getString("CategoryGui.Return", "&6戻る &8| &6Return");

        // フッター
        footerItemStacks = Map.of(45, createFooterItem(Material.ARROW, 1, prevPage), 46,
                createFooterItem(Material.RED_STAINED_GLASS_PANE, 64, decrease64), 47,
                createFooterItem(Material.RED_STAINED_GLASS_PANE, 8, decrease8), 48,
                createFooterItem(Material.RED_STAINED_GLASS_PANE, 1, decrease1), 49,
                createFooterItem(Material.OAK_DOOR, 1, back), 50,
                createFooterItem(Material.BLUE_STAINED_GLASS_PANE, 1, increase1), 51,
                createFooterItem(Material.BLUE_STAINED_GLASS_PANE, 8, increase8), 52,
                createFooterItem(Material.BLUE_STAINED_GLASS_PANE, 64, increase64), 53,
                createFooterItem(Material.ARROW, 1, nextPage));
    }

    /**
     * フッターに使うアイテムを作成する。
     *
     * @author akaregi
     * @since v1.1.0
     *
     * @param material    アイテムの種類。
     * @param stackAmount アイテムの量。
     * @param displayName 表示名。
     *
     * @return メタ情報(パラメタ)を適用したアイテム。
     */
    @NotNull
    private static ItemStack createFooterItem(@NotNull Material material, int stackAmount,
            @NotNull String displayName) {
        ItemStack item = new ItemStack(material, stackAmount);
        Optional<ItemMeta> itemMeta = Optional.ofNullable(item.getItemMeta());

        itemMeta.ifPresent(meta -> {
            meta.setDisplayName(displayName.replaceAll("&([a-f0-9])", "§$1"));
            item.setItemMeta(meta);
        });

        return item;
    }

    /**
     * {@code String} から {@code Sound} に変換する。
     *
     * @author akaregi
     * @since v1.1.0
     *
     * @param sound 変換元
     *
     * @return {@code Optional<Sound>}
     */
    @NotNull
    private static Optional<Sound> getSound(@Nullable String sound) {
        if (sound == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Sound.valueOf(sound));
        } catch (IllegalArgumentException e) {
            // No such sound!
            e.printStackTrace();

            return Optional.empty();
        }
    }
}
