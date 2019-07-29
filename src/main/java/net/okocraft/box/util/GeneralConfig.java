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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.val;
import net.md_5.bungee.api.ChatColor;
import net.okocraft.box.Box;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.database.Database;
import net.okocraft.box.gui.CategorySelectorGUI;

/**
 * @author LazyGon
 */
public class GeneralConfig {
    private final Box plugin;
    private final Database database;

    @Getter
    private final CustomConfig itemCustomConfig;

    // FileConfiguration
    @Getter
    private FileConfiguration defaultConfig;
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

    @Getter
    private List<World> disabledWorlds;

    @Getter
    private List<World> replantWorlds;

    // Item Template
    @Getter
    private String itemTemplateName;
    @Getter
    private List<String> itemTemplateLore;

    // Item categories
    @Getter
    private Map<String, ConfigurationSection> categories;
    @Getter
    private Map<String, String> categoryGuiNameMap;
    @Getter
    private List<String> allItems;
    @Getter
    private Map<String, Integer> sellPrice;

    // CategorySelectionGui Name
    @Getter
    private String categorySelectionGuiName;

    // CategoryGui
    @Getter
    private String categoryGuiName;
    @Getter
    private Map<Integer, ItemStack> footerItemStacks;

    public GeneralConfig(Database database) {
        this.plugin = Box.getInstance();
        this.database = database;

        itemCustomConfig = new CustomConfig(plugin, "items.yml");

        defaultConfig = plugin.getConfig();
        itemConfig = itemCustomConfig.getConfig();

        // Create some files
        plugin.saveDefaultConfig();
        itemCustomConfig.saveDefaultConfig();

        // Bind variables
        initConfig();
    }

    public void reload() {
        itemCustomConfig.initConfig();

        defaultConfig = plugin.getConfig();
        itemConfig = itemCustomConfig.getConfig();

        initConfig();

        allItems.forEach(name -> {
            database.addColumn(name, "INTEGER", "0", false);
            database.addColumn("autostore_" + name, "TEXT", "false", false);
        });

        plugin.registerEvents();
    }

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

        itemTemplateName = Optional.ofNullable(itemConfig.getString("ItemTemplate.display_name"))
                .orElse("&6%item_jp% &8| &6%item_en%").replaceAll("&([a-f0-9])", "§$1");

        itemTemplateLore = itemConfig.getStringList("ItemTemplate.lore").stream()
                .map(line -> line.replaceAll("&([a-f0-9])", "§$1")).collect(Collectors.toList());

        // コンフィグに書かれた順番で表示するためにLinkedHashMapを使っている。
        categories = new LinkedHashMap<>();
        if (itemConfig.isConfigurationSection("categories")) {
            itemConfig.getConfigurationSection("categories").getKeys(false).forEach(sectionName -> {
                if (itemConfig.isConfigurationSection("categories." + sectionName)) {
                    ConfigurationSection section = itemConfig.getConfigurationSection("categories." + sectionName);
                    categories.put(sectionName, section);
                }
            });
        }

        // CHANGED: Nullable になると IntelliJ がうるさいので Optional 化
        categorySelectionGuiName = ChatColor.translateAlternateColorCodes('&',
                Optional.ofNullable(itemConfig.getString("CategorySelectionGui.GuiName")).orElse("ボックス - カテゴリー選択"));

        categoryGuiName = ChatColor.translateAlternateColorCodes('&',
                Optional.ofNullable(itemConfig.getString("CategoryGui.GuiName")).orElse("ボックス - %category%"));

        allItems = new ArrayList<>();
        categoryGuiNameMap = new HashMap<>();

        categories.forEach((category, section) -> {
            Optional.ofNullable(section.getString("display_name"))
                    .ifPresent(name -> categoryGuiNameMap.put(category, categoryGuiName
                            .replaceAll("%category%", category).replaceAll("%category_item_display_name%", name)));

            Optional.ofNullable(section.getConfigurationSection("item"))
                    .ifPresent(items -> allItems.addAll(items.getKeys(false).stream()
                            .filter(itemName -> Material.getMaterial(itemName) != null).collect(Collectors.toList())));
        });

        val priceConfig = new CustomConfig(plugin, "sellprice.yml");
        priceConfig.saveDefaultConfig();
        sellPrice = allItems.stream().collect(Collectors.toMap(itemName -> itemName,
                itemName -> priceConfig.getConfig().getInt(itemName), (i1, i2) -> i1, LinkedHashMap::new));
    }

    public void addCategory() {
        itemCustomConfig.initConfig();
        itemConfig = itemCustomConfig.getConfig();
        if (!itemConfig.isConfigurationSection("categories")) {
            return;
        }
        itemConfig.getConfigurationSection("categories").getKeys(false).stream()
                .filter(sectionName -> !categories.containsKey(sectionName))
                .filter(sectionName -> itemConfig.isConfigurationSection("categories." + sectionName))
                .forEach(sectionName -> {
                    ConfigurationSection section = itemConfig.getConfigurationSection("categories." + sectionName);
                    categories.put(sectionName, section);
                    Optional.ofNullable(section.getString("display_name"))
                            .ifPresent(name -> categoryGuiNameMap.put(sectionName,
                                    categoryGuiName.replaceAll("%category%", sectionName)
                                            .replaceAll("%category_item_display_name%", name)));

                    Optional.ofNullable(section.getConfigurationSection("item"))
                            .ifPresent(items -> allItems.addAll(items.getKeys(false).stream()
                                    .filter(itemName -> Material.getMaterial(itemName) != null)
                                    .peek(itemName -> {
                                        database.addColumn(itemName, "INTEGER", "0", false);
                                        database.addColumn("autostore_" + itemName, "TEXT", "false", false);
                                    }).collect(Collectors.toList())));
                });

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
        val DEFAULT_SOUND_VOLUME = 1.0;
        val DEFAULT_SOUND_PITCH = 1.0;

        val DEFAULT_OPEN_SOUND = Sound.BLOCK_CHEST_OPEN;
        val DEFAULT_TAKE_IN_SOUND = Sound.ENTITY_ITEM_PICKUP;
        val DEFAULT_TAKE_OUT_SOUND = Sound.BLOCK_STONE_BUTTON_CLICK_ON;
        val DEFAULT_NOT_ENOUGH_SOUND = Sound.ENTITY_ENDERMAN_TELEPORT;
        val DEFAULT_INCREASE_SOUND = Sound.BLOCK_TRIPWIRE_CLICK_ON;
        val DEFAULT_DECREASE_SOUND = Sound.BLOCK_TRIPWIRE_CLICK_OFF;
        val DEFAULT_CHANGE_PAGE_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        val DEFAULT_RETURN_TO_SELECTION_GUI_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

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
        autoStoreEnabled = defaultConfig.getBoolean("General.AutoStore.Enabled", false);
        autoStoreEnabledByDefault = defaultConfig.getBoolean("General.AutoStore.PlayerDefault", false);
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
        disabledWorlds = defaultConfig.getStringList("General.DisabledWorld").stream().map(Bukkit::getWorld)
                .filter(Objects::nonNull).collect(Collectors.toList());
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
        replantWorlds = defaultConfig.getStringList("General.ReplantWorld").stream().map(Bukkit::getWorld)
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
        val prevPage = itemConfig.getString("CategoryGui.PreviousPage", "&6前のページ &8| &6Prev Page");
        val nextPage = itemConfig.getString("CategoryGui.NextPage", "&6次のページ &8| &6Nex Page");

        // 取扱単位（減算）
        val decrease1 = itemConfig.getString("CategoryGui.Decrease1", "&7単位: &c-1");
        val decrease8 = itemConfig.getString("CategoryGui.Decrease8", "&7単位: &c-8");
        val decrease64 = itemConfig.getString("CategoryGui.Decrease64", "&7単位: &c-64");

        // 取扱単位（加算）
        val increase1 = itemConfig.getString("CategoryGui.Increase1", "&7単位: &b+1");
        val increase8 = itemConfig.getString("CategoryGui.Increase8", "&7単位: &b+8");
        val increase64 = itemConfig.getString("CategoryGui.Increase64", "&7単位: &b+64");

        // 戻る
        val back = itemConfig.getString("CategoryGui.Return", "&6戻る &8| &6Return");

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
    @Nonnull
    private static ItemStack createFooterItem(Material material, int stackAmount, String displayName) {
        val item = new ItemStack(material, stackAmount);
        val itemMeta = Optional.ofNullable(item.getItemMeta());

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
    @Nonnull
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
