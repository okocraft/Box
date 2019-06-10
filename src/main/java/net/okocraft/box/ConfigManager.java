package net.okocraft.box;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.val;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.database.Database;

public class ConfigManager {
    private Box plugin;
    private Database database;

    // CustomConfig
    private CustomConfig messageCustomConfig;
    private CustomConfig storingItemCustomConfig;

    // FIleConfiguration
    @Getter
    private FileConfiguration defaultConfig;
    @Getter
    private FileConfiguration messageConfig;
    @Getter
    private FileConfiguration storingItemConfig;

    // fields
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
    private Sound returnToSelectionGuiSound;
    @Getter
    private Sound changePageSound;

    // autoStoreSetting
    @Getter
    private boolean autoStoreEnabled;
    @Getter
    private boolean autoStoreEnabledByDefault;

    @Getter
    private List<World> disabledWorlds;

    // Item Template
    @Getter
    private String itemTemplateName;
    @Getter
    private List<String> itemTemplateLore;

    // Item categories
    @Getter
    private Map<String, MemorySection> categories;
    @Getter
    private Map<String, String> categoryGuiNameMap;
    @Getter
    private List<String> allItems;

    // CategorySelectionGui Name
    @Getter
    private String categorySelectionGuiName;

    // CategoryGui
    @Getter
    private String categoryGuiName;
    @Getter
    private Map<Integer, ItemStack> footerItemStacks;

    ConfigManager(Database database) {
        this.plugin = Box.getInstance();
        this.database = database;

        // Initialize configuration
        messageCustomConfig     = new CustomConfig(plugin, "messages.yml");
        storingItemCustomConfig = new CustomConfig(plugin, "items.yml");

        defaultConfig     = plugin.getConfig();
        messageConfig     = messageCustomConfig.getConfig();
        storingItemConfig = storingItemCustomConfig.getConfig();

        // Create some files
        plugin.saveDefaultConfig();
        messageCustomConfig.saveDefaultConfig();
        storingItemCustomConfig.saveDefaultConfig();

        // Bind variables
        initConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        messageCustomConfig.initConfig();
        storingItemCustomConfig.initConfig();

        defaultConfig     = plugin.getConfig();
        messageConfig     = messageCustomConfig.getConfig();
        storingItemConfig = storingItemCustomConfig.getConfig();

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

        // Footer
        initFooterConfig();

        // CHANGED: Nullable になると IntelliJ がうるさいので Optional 化
        itemTemplateName = Optional.ofNullable(storingItemConfig.getString("ItemTemplate.display_name"))
                .orElse("&6%item_jp% &8| &6%item_en%")
                .replaceAll("&([a-f0-9])", "§$1");

        itemTemplateLore = storingItemConfig.getStringList("ItemTemplate.lore").stream()
                .map(line -> line.replaceAll("&([a-f0-9])", "§$1"))
                .collect(Collectors.toList());

        // FIXME: new LinkedHashMap<T>? Is it needed to sort?
        categories = new LinkedHashMap<>();

        // TODO: MemorySection ってなんやねん......
        val categoryConfig = getMemorySection(storingItemConfig.get("categories"));
        if (categoryConfig != null) {
            categoryConfig.getValues(false).forEach((sectionName, sectionObject) -> {
                MemorySection section = getMemorySection(sectionObject);
                if (section != null) {
                    categories.put(sectionName, section);
                }
            });
        }

        // CHANGED: Nullable になると IntelliJ がうるさいので Optional 化
        categorySelectionGuiName = Optional.ofNullable(storingItemConfig.getString("CategorySelectionGui.GuiName"))
                .orElse("アイテムボックス - カテゴリー選択")
                .replaceAll("&([a-f0-9])", "§$1");

        categoryGuiName = Optional.ofNullable(storingItemConfig.getString("CategoryGui.GuiName"))
                .orElse("アイテムボックス - %category%")
                .replaceAll("&([a-f0-9])", "§$1");;

        // FIXME: new ArrayList<T>, new HashMap<T>? ...
        allItems           = new ArrayList<>();
        categoryGuiNameMap = new HashMap<>();

        categories.forEach((category, memorySection) -> {
            // CHANGED: Null の処理に Optional を使う
            val displayName = Optional.ofNullable(memorySection.getString("display_name"));
            displayName.ifPresent(name ->
                    categoryGuiNameMap.put(
                        category,
                        categoryGuiName
                            .replaceAll("%category%", category)
                            .replaceAll("%category_item_display_name%", name)
                            .replaceAll("&([a-f0-9])", "§$1")
                    )
            );

            // CHANGED: Null の処理に Optional を使う
            val categoryItems = Optional.ofNullable(getMemorySection(memorySection.get("item")));
            categoryItems.ifPresent(items ->
                    allItems.addAll(items.getKeys(false).stream()
                    .filter(itemName -> Material.getMaterial(itemName) != null)
                    .collect(Collectors.toList()))
            );
        });
    }

    /**
     * 音設定初期化
     *
     * @see ConfigManager#initConfig()
     */
    private void initSoundConfig() {
        // CHANGED: DON'T USE MAGIC NUMBER
        val DEFAULT_SOUND_VOLUME = 1.0;
        val DEFAULT_SOUND_PITCH  = 1.0;

        soundVolume     = (float) defaultConfig.getDouble("General.SoundSetting.Volume", DEFAULT_SOUND_VOLUME);
        soundPitch      = (float) defaultConfig.getDouble("General.SoundSetting.Pitch", DEFAULT_SOUND_PITCH);
        // FIXME: 以下の設定は Nullable, デフォルト設定必要か
        openSound       = getSound(defaultConfig.getString("General.SoundSetting.Sounds.Open"));
        takeInSound     = getSound(defaultConfig.getString("General.SoundSetting.Sounds.TakeIn"));
        takeOutSound    = getSound(defaultConfig.getString("General.SoundSetting.Sounds.TakeOut"));
        notEnoughSound  = getSound(defaultConfig.getString("General.SoundSetting.Sounds.NotEnough"));
        decreaseSound   = getSound(defaultConfig.getString("General.SoundSetting.Sounds.Decrease"));
        increaseSound   = getSound(defaultConfig.getString("General.SoundSetting.Sounds.Increase"));
        changePageSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.ChangePage"));
        returnToSelectionGuiSound = getSound(defaultConfig.getString("General.SoundSetting.Sounds.ReturnToSelectionGui"));
    }

    /**
     * AutoStore 設定初期化
     *
     * @see ConfigManager#initConfig()
     */
    private void initAutoStoreConfig() {
        autoStoreEnabled = defaultConfig.getBoolean("General.AutoStore.Enabled", false);
        autoStoreEnabledByDefault = defaultConfig.getBoolean("General.AutoStore.PlayerDefault", false);
    }

    /**
     * DisabledWorld 設定初期化
     *
     * @see ConfigManager#initConfig()
     */
    private void initDisabledWorldConfig() {
        disabledWorlds = defaultConfig.getStringList("General.DisabledWorld").stream()
                .map(Bukkit::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * フッター初期化
     *
     * @see ConfigManager#initConfig()
     */
    private void initFooterConfig() {
        // ページ送り
        // FIXME: path の Previouspage → PreviousPage (キャメルケース化)
        // FIXME: path の Nextpage → NextPage (キャメルケース化)
        val prevPage = storingItemConfig.getString("CategoryGui.Previouspage", "&6前のページ &8| &6Prev Page");
        val nextPage = storingItemConfig.getString("CategoryGui.Nextpage", "&6次のページ &8| &6Nex Page");

        // 取扱単位（減算）
        val decrease1  = storingItemConfig.getString("CategoryGui.Decrease1", "&7単位: &c-1");
        val decrease8  = storingItemConfig.getString("CategoryGui.Decrease8", "&7単位: &c-8");
        val decrease64 = storingItemConfig.getString("CategoryGui.Decrease64", "&7単位: &c-64");

        // 取扱単位（加算）
        val increase1  = storingItemConfig.getString("CategoryGui.Increase1", "&7単位: &b+1");
        val increase8  = storingItemConfig.getString("CategoryGui.Increase8", "&7単位: &b+8");
        val increase64 = storingItemConfig.getString("CategoryGui.Increase64", "&7単位: &b+64");

        // 戻る
        val back = storingItemConfig.getString("CategoryGui.Return", "&6戻る &8| &6Return");

        // CHANGED: Map<K, V> の初期化に Java 9 から存在する Map#of を使う
        // CHANGED: 長ったらしいので表示名を変数に置換
        footerItemStacks = Map.of(
                45, createFooterItem(Material.ARROW,                   1,  prevPage),
                46, createFooterItem(Material.RED_STAINED_GLASS_PANE,  64, decrease64),
                47, createFooterItem(Material.RED_STAINED_GLASS_PANE,  8,  decrease8),
                48, createFooterItem(Material.RED_STAINED_GLASS_PANE,  1,  decrease1),
                49, createFooterItem(Material.OAK_DOOR,                1,  back),
                50, createFooterItem(Material.BLUE_STAINED_GLASS_PANE, 1,  increase1),
                51, createFooterItem(Material.BLUE_STAINED_GLASS_PANE, 8,  increase8),
                52, createFooterItem(Material.BLUE_STAINED_GLASS_PANE, 64, increase64),
                53, createFooterItem(Material.ARROW,                   1,  nextPage)
        );
    }

    /**
     * フッターに使うアイテムを作成する。
     *
     * @param material    アイテムの種類。
     * @param stackAmount アイテムの量。
     * @param displayName  表示名。
     *
     * @return メタ情報(パラメタ)を適用したアイテム。
     */
    @Nonnull
    private static ItemStack createFooterItem(Material material, int stackAmount, String displayName) {
        val hooterItem = new ItemStack(material, stackAmount);

        // CHANGED: そのまま処理すると Nullable で警告されるので Optional
        val hooterItemMeta = Optional.ofNullable(hooterItem.getItemMeta());

        hooterItemMeta.ifPresent(meta -> {
            meta.setDisplayName(displayName.replaceAll("&([a-f0-9])", "§$1"));
            hooterItem.setItemMeta(meta);
        });

        return hooterItem;
    }

    /**
     * String から Sound に変換を試みる。変換先が見つからないないし失敗した場合は @code{null} を返す。
     *
     * @param sound Sound にしたい String
     *
     * @return Sound, 変換に失敗した場合 null
     */
    @Nullable
    private static Sound getSound(String sound) {
        try {
            return Sound.valueOf(sound);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }

    @Nullable
    public static MemorySection getMemorySection(Object object) {
        if (object == null) {
            return null;
        }

        if (object.getClass().getSimpleName().equals("MemorySection")) {
            return (MemorySection) object;
        }

        return null;
    }
}
