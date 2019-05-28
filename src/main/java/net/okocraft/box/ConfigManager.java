package net.okocraft.box;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import lombok.Getter;
import net.okocraft.box.database.Database;

public class ConfigManager {

    private Box instance;

    private Database database;

    // CustomConfig
    private CustomConfig messageCustomConfig;
    private CustomConfig storingItemCustomConfig;

    // FIleConfiguration
    @Getter private FileConfiguration defaultConfig;
    @Getter private FileConfiguration messageConfig;
    @Getter private FileConfiguration storingItemConfig;

    // fields

    @Getter private float soundPitch = 0.0F;
    @Getter private float soundVolume = 0.0F;
    @Getter private Sound openSound;
    @Getter private Sound takeInSound;
    @Getter private Sound takeOutSound;
    @Getter private Sound notEnoughSound;
    @Getter private Sound decreaseSound;
    @Getter private Sound increaseSound;
    @Getter private Sound returnToSelectionGuiSound;
    @Getter private Sound changePageSound;

    // autoStoreSetting
    @Getter private boolean autoStoreEnabled;
    @Getter private boolean autoStoreEnabledByDefault;

    @Getter private List<World> disabledWorlds;

    // Item Template
    @Getter private String itemTemplateName;
    @Getter private List<String> itemTemplateLore;

    // Item categories
    @Getter private Map<String, MemorySection> categories;
    @Getter private Map<String, String> categoryGuiNameMap;
    @Getter private List<String> allItems;

    // CategorySelectionGui Name
    @Getter private String categorySelectionGuiName;

    // CategoryGui
    @Getter private String categoryGuiName;
    @Getter private Map<Integer, ItemStack> footerItemStacks;


    public ConfigManager(Plugin plugin, Database database) {
        instance = Box.getInstance();
        this.database = database;
        defaultConfig = instance.getConfig();
        messageCustomConfig = new CustomConfig(instance, "messages.yml");
        storingItemCustomConfig = new CustomConfig(instance, "items.yml");
        messageConfig = messageCustomConfig.getConfig();
        storingItemConfig = storingItemCustomConfig.getConfig();
        instance.saveDefaultConfig();
        messageCustomConfig.saveDefaultConfig();
        storingItemCustomConfig.saveDefaultConfig();
        loadFields();
    }

    public void reloadConfig() {
        instance.reloadConfig();
        messageCustomConfig.reloadConfig();
        storingItemCustomConfig.reloadConfig();
        defaultConfig = instance.getConfig();
        messageConfig = messageCustomConfig.getConfig();
        storingItemConfig = storingItemCustomConfig.getConfig();

        loadFields();
        allItems.forEach(itemName -> {
            database.addColumn(itemName, "INTEGER", "0", false);
            database.addColumn("autostore_" + itemName, "TEXT", "false", false);
        });
        instance.registerEvents();
    }

    private void loadFields() {
        soundVolume = (float) defaultConfig.getDouble("General.SoundSetting.Volume", 1.0);
        soundPitch = (float) defaultConfig.getDouble("General.SoundSetting.Pitch", 1.0);
        openSound = soundOrNull(defaultConfig.getString("General.SoundSetting.Sounds.Open"));
        takeInSound = soundOrNull(defaultConfig.getString("General.SoundSetting.Sounds.TakeIn"));
        takeOutSound = soundOrNull(defaultConfig.getString("General.SoundSetting.Sounds.TakeOut"));
        notEnoughSound = soundOrNull(defaultConfig.getString("General.SoundSetting.Sounds.NotEnough"));
        decreaseSound = soundOrNull(defaultConfig.getString("General.SoundSetting.Sounds.Decrease"));
        increaseSound = soundOrNull(defaultConfig.getString("General.SoundSetting.Sounds.Increase"));
        returnToSelectionGuiSound = soundOrNull(defaultConfig.getString("General.SoundSetting.Sounds.ReturnToSelectionGui"));
        changePageSound = soundOrNull(defaultConfig.getString("General.SoundSetting.Sounds.ChangePage"));

        autoStoreEnabled = defaultConfig.getBoolean("General.AutoStore.Enabled", false);
        autoStoreEnabledByDefault = defaultConfig.getBoolean("General.AutoStore.PlayerDefault", false);

        disabledWorlds = defaultConfig.getStringList("General.DisabledWorld").stream()
                .map(Bukkit::getWorld)
                .filter(world -> world != null)
                .collect(Collectors.toList());

        itemTemplateName = storingItemConfig.getString("ItemTemplate.display_name", "&6%item_jp% &8| &6%item_en%").replaceAll("&([a-f0-9])", "§$1");
        itemTemplateLore = storingItemConfig.getStringList("ItemTemplate.lore").stream().map(loreLine -> loreLine.replaceAll("&([a-f0-9])", "§$1"))
                .collect(Collectors.toList());

        categories = new LinkedHashMap<>();
        MemorySection categoryConfig = memorySectionOrNull(storingItemConfig.get("categories"));
        if (categoryConfig != null) {
            categoryConfig.getValues(false).forEach((sectionName, sectionObject) -> {
                MemorySection section = memorySectionOrNull(sectionObject);
                if (section != null) {
                    categories.put(sectionName, section);
                }
            });
        }

        categorySelectionGuiName = storingItemConfig.getString("CategorySelectionGui.GuiName", "アイテムボックス - カテゴリー選択").replaceAll("&([a-f0-9])", "§$1");
        categoryGuiName = storingItemConfig.getString("CategoryGui.GuiName", "アイテムボックス - %category%").replaceAll("&([a-f0-9])", "§$1");

        allItems = new ArrayList<>();
        categoryGuiNameMap = new HashMap<>();
        categories.forEach((category, memorySection) -> {

            String displayName = memorySection.getString("display_name");
            if (displayName != null)
                categoryGuiNameMap.put(category, categoryGuiName.replaceAll("%category%", category).replaceAll("%category_item_display_name%", displayName).replaceAll("&([a-f0-9])", "§$1"));

            MemorySection categoryItems = memorySectionOrNull(memorySection.get("item"));
            if (categoryItems != null) {
                allItems.addAll(categoryItems.getKeys(false).stream()
                        .filter(itemName -> Material.getMaterial(itemName) != null)
                        .collect(Collectors.toList()));
            }
        });

        footerItemStacks = new HashMap<>();

        footerItemStacks.put(45, createFooter(Material.ARROW, 1,  storingItemConfig.getString("CategoryGui.Previouspage", "&6前のページ &8| &6Prev Page")));
        footerItemStacks.put(46, createFooter(Material.RED_STAINED_GLASS_PANE, 64, storingItemConfig.getString("CategoryGui.Decrease64", "&7単位: &c-64")));
        footerItemStacks.put(47, createFooter(Material.RED_STAINED_GLASS_PANE, 8, storingItemConfig.getString("CategoryGui.Decrease8", "&7単位: &c-8")));
        footerItemStacks.put(48, createFooter(Material.RED_STAINED_GLASS_PANE, 1, storingItemConfig.getString("CategoryGui.Decrease1", "&7単位: &c-1")));
        footerItemStacks.put(49, createFooter(Material.OAK_DOOR, 1, storingItemConfig.getString("CategoryGui.Return", "&6戻る &8| &6Return")));
        footerItemStacks.put(50, createFooter(Material.BLUE_STAINED_GLASS_PANE, 1, storingItemConfig.getString("CategoryGui.Increase1", "&7単位: &b+1")));
        footerItemStacks.put(51, createFooter(Material.BLUE_STAINED_GLASS_PANE, 8, storingItemConfig.getString("CategoryGui.Increase8", "&7単位: &b+8")));
        footerItemStacks.put(52, createFooter(Material.BLUE_STAINED_GLASS_PANE, 64, storingItemConfig.getString("CategoryGui.Increase64", "&7単位: &b+64")));
        footerItemStacks.put(53, createFooter(Material.ARROW, 1, storingItemConfig.getString("CategoryGui.Nextpage", "&6次のページ &8| &6Nex Page")));
    }

    private static ItemStack createFooter(Material material, int stackAmount, String displayName) {
        ItemStack hooterItem = new ItemStack(material, stackAmount);
        ItemMeta hooterItemMeta = hooterItem.getItemMeta();
        hooterItemMeta.setDisplayName(displayName.replaceAll("&([a-f0-9])", "§$1"));
        hooterItem.setItemMeta(hooterItemMeta);
        return hooterItem;
    }

    public static Sound soundOrNull(String sound) {
        try {
            return Sound.valueOf(sound);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }

    public static MemorySection memorySectionOrNull(Object obj) {
        if (obj == null) return null;
        if (obj.getClass().getSimpleName().equals("MemorySection"))
            return (MemorySection) obj;
        return null;
    }
}
