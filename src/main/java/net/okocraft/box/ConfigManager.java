package net.okocraft.box;

import java.util.ArrayList;
import java.util.HashMap;
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

public class ConfigManager {

    private Box instance;

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


    public ConfigManager(Plugin plugin) {
        instance = Box.getInstance();
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

        categories = new HashMap<>();
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

        ItemStack previousPage = new ItemStack(Material.ARROW);
        ItemMeta previousPageMeta = previousPage.getItemMeta();
        previousPageMeta.setDisplayName(storingItemConfig.getString("CategoryGui.Previouspage", "&6前のページ &8| &6Prev Page").replaceAll("&([a-f0-9])", "§$1"));
        previousPage.setItemMeta(previousPageMeta);
        footerItemStacks.put(45, previousPage);

        ItemStack decrease64 = new ItemStack(Material.RED_STAINED_GLASS_PANE, 64);
        ItemMeta decrease64Meta = decrease64.getItemMeta();
        decrease64Meta.setDisplayName(storingItemConfig.getString("CategoryGui.Decrease64", "&7単位: &c-64").replaceAll("&([a-f0-9])", "§$1"));
        decrease64.setItemMeta(decrease64Meta);
        footerItemStacks.put(46, decrease64);

        ItemStack decrease8 = new ItemStack(Material.RED_STAINED_GLASS_PANE, 8);
        ItemMeta decrease8Meta = decrease8.getItemMeta();
        decrease8Meta.setDisplayName(storingItemConfig.getString("CategoryGui.Decrease8", "&7単位: &c-8").replaceAll("&([a-f0-9])", "§$1"));
        decrease8.setItemMeta(decrease8Meta);
        footerItemStacks.put(47, decrease8);

        ItemStack decrease1 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta decrease1Meta = decrease1.getItemMeta();
        decrease1Meta.setDisplayName(storingItemConfig.getString("CategoryGui.Decrease1", "&7単位: &c-1").replaceAll("&([a-f0-9])", "§$1"));
        decrease1.setItemMeta(decrease1Meta);
        footerItemStacks.put(48, decrease1);

        ItemStack returnGui = new ItemStack(Material.OAK_DOOR);
        ItemMeta returnGuiMeta = returnGui.getItemMeta();
        returnGuiMeta.setDisplayName(storingItemConfig.getString("CategoryGui.Return", "&6戻る &8| &6Return").replaceAll("&([a-f0-9])", "§$1"));
        returnGui.setItemMeta(returnGuiMeta);
        footerItemStacks.put(49, returnGui);

        ItemStack increase1 = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta increase1Meta = increase1.getItemMeta();
        increase1Meta.setDisplayName(storingItemConfig.getString("CategoryGui.Increase1", "&7単位: &b+1").replaceAll("&([a-f0-9])", "§$1"));
        increase1.setItemMeta(increase1Meta);
        footerItemStacks.put(50, increase1);

        ItemStack increase8 = new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 8);
        ItemMeta increase8Meta = increase8.getItemMeta();
        increase8Meta.setDisplayName(storingItemConfig.getString("CategoryGui.Increase8", "&7単位: &b+8").replaceAll("&([a-f0-9])", "§$1"));
        increase8.setItemMeta(increase8Meta);
        footerItemStacks.put(51, increase8);

        ItemStack increase64 = new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 64);
        ItemMeta increase64Meta = increase64.getItemMeta();
        increase64Meta.setDisplayName(storingItemConfig.getString("CategoryGui.Increase64", "&7単位: &b+64").replaceAll("&([a-f0-9])", "§$1"));
        increase64.setItemMeta(increase64Meta);
        footerItemStacks.put(52, increase64);

        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.setDisplayName(storingItemConfig.getString("CategoryGui.Nextpage", "&6次のページ &8| &6Nex Page").replaceAll("&([a-f0-9])", "§$1"));
        nextPage.setItemMeta(nextPageMeta);
        footerItemStacks.put(53, nextPage);

    }

    public static Sound soundOrNull(String sound) {
        try {
            return Sound.valueOf(sound);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }

    public static MemorySection memorySectionOrNull(Object obj) {
        if (obj.getClass().getSimpleName().equals("MemorySection"))
            return (MemorySection) obj;
        return null;
    }
}
