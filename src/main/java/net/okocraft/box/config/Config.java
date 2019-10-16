
package net.okocraft.box.config;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.okocraft.box.Box;

public final class Config {

    private static Box plugin = Box.getInstance();
    private static FileConfiguration config = plugin.getConfig();

    private Config() {
    }

    public static class CategorySelectionGui {
        public static String getName() {
            return get().getString("gui.category-selection-gui.title", "カテゴリー選択");
        }

        public static String getItemNameFormat() {
            return get().getString("gui.category-selection-gui.item-format.display-name", "&6%display-name% &8| &6%category-name%");
        }

        public static List<String> getItemLoreFormat() {
            return get().getStringList("gui.category-selection-gui.item-format.lore");
        }
    }

    public static String getCategoryGuiTitle() {
        return get().getString("gui.category-gui-title", "%category-name%");
    }

    public static class TransactionGui {
        public static String getItemNameFormat() {
            return get().getString("gui.transaction-item-format.display-name", "&6%item-name% &8| &6%category-name%");
        }

        public static List<String> getItemLoreFormat() {
            return get().getStringList("gui.transaction-item-format.lore");
        }
    }

    public static class BuyAndSellGui {
        public static String getItemNameFormat() {
            return get().getString("gui.buy-and-sell-item-format.display-name", "&6%item-name% &8| &6%category-name%");
        }

        public static List<String> getItemLoreFormat() {
            return get().getStringList("gui.buy-and-sell-item-format.lore");
        }
    }

    public static class CraftGui {
        public static String getItemNameFormat() {
            return get().getString("gui.craft-item-format.display-name", "&6%item-name% &8| &6%category-name%");
        }

        public static List<String> getItemLoreFormat() {
            return get().getStringList("gui.craft-item-format.lore");
        }

        public static String getItemRecipeLineFormat() {
            return get().getString("gui.craft-item-format.materials-placeholder-format", "  %material%: %amount%");
        }
    }

    public static enum PageFunctionItems {
        PREVIOUS_PAGE(Material.ARROW),
        DECREASE(Material.RED_STAINED_GLASS_PANE),
        CHANGE_UNIT(Material.PURPLE_STAINED_GLASS_PANE),
        INCREASE(Material.BLUE_STAINED_GLASS_PANE),
        BACK_MENU(Material.OAK_DOOR),
        TRANSACTION(Material.CHEST),
        BUY_AND_SELL(Material.GOLD_NUGGET),
        CRAFT(Material.CRAFTING_TABLE),
        NEXT_PAGE(Material.ARROW);

        private final Material material;
        private ItemStack item;

        private PageFunctionItems(Material material) {
            this.material = material;
            init();
        }

        public ItemStack get() {
            if (item == null) {
                init();
            }
            return item.clone();
        }

        public String getDisplayName() {
            String key = "gui.page-function-items." + toString() + ".display-name";
            return ChatColor.translateAlternateColorCodes('&', Config.get().getString(key, key));
        }
        
        public List<String> getLore() {
            String key = "gui.page-function-items." + toString() + ".lore";
            List<String> lore = Config.get().getStringList(key);
            lore.replaceAll(loreLine -> ChatColor.translateAlternateColorCodes('&', loreLine));
            return lore;
        }

        private void init() {
            item = createPageFunctionItem(material, getDisplayName(), getLore());
        }

        private static void reload() {
            for (PageFunctionItems pageFunctionItem : values()) {
                pageFunctionItem.init();
            }
        }

        /**
         * フッターに使うアイテムを作成する。
         *
         * @param material    アイテムの種類。
         * @param displayName 表示名。
         * @param lore        アイテムの説明文
         * @return メタ情報(パラメタ)を適用したアイテム。
         * @author akaregi
         * @since v1.1.0
         */
        private static ItemStack createPageFunctionItem(Material material, String displayName, List<String> lore) {
            if (material == null) {
                return new ItemStack(Material.AIR);
            }
            return new ItemStack(material) {
                {
                    ItemMeta meta = getItemMeta();
                    meta.setDisplayName(displayName);
                    meta.setLore(lore);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    setItemMeta(meta);
                }
            };
        }

        @Override
        public String toString() {
            return name().replace("_", "-").toLowerCase(Locale.ROOT);
        }
    }

    public static enum Sounds {
        OPEN(Sound.BLOCK_CHEST_OPEN),
        DEPOSIT(Sound.ENTITY_ITEM_PICKUP),
        WITHDRAW(Sound.BLOCK_STONE_BUTTON_CLICK_ON),
        BUY(Sound.ENTITY_ITEM_PICKUP),
        SELL(Sound.BLOCK_STONE_BUTTON_CLICK_ON),
        NOT_ENOUGH(Sound.ENTITY_ENDERMAN_TELEPORT),
        DECREASE_UNIT(Sound.BLOCK_TRIPWIRE_CLICK_OFF),
        INCREASE_UNIT(Sound.BLOCK_TRIPWIRE_CLICK_ON),
        MENU_BACK(Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
        CHANGE_PAGE(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);

        private Sound defaultSound;

        Sounds(Sound def) {
            this.defaultSound = def;
        }
        
        public Sound getSound() {
            try {
                return Sound.valueOf(get().getString("sound-settings." + toString() + ".sound", ""));
            } catch (IllegalArgumentException e) {
                return defaultSound;
            }
        }

        public float getMaxPitch() {
            return getSoundProperty(true, true);
        }

        public float getMinPitch() {
            return getSoundProperty(true, false);
        }

        public float getMaxVolume() {
            return getSoundProperty(false, true);
        }

        public float getMinVolume() {
            return getSoundProperty(false, false);
        }
        
        public float getPitchRandomly() {
            return getSoundPropertyValueRandomly(true);
        }
        
        public float getVolumeRandomly() {
            return getSoundPropertyValueRandomly(false);
        }
        
        /**
         * @param isPitch If true, this method gets pitch, otherwise volume.
         * @param isMax If true, this method gets max value, otherwise min value.
         * @return property for the sound.
         */
        private float getSoundProperty(boolean isPitch, boolean isMax) {
            List<Double> property = get().getDoubleList("sound-settings." + toString() + (isPitch ? ".pitch" : ".volume"));
            if (property.size() != 2) {
                return isMax ? 1.25F : 0.75F;
            }
    
            return Math.max(Math.min((float) property.get(isMax ? 1 : 0).doubleValue(), 2.0F), 0F);
        }
        
        private static final Random random = new Random();
        private float getSoundPropertyValueRandomly(boolean isPitch) {            
            float max = isPitch ? getMaxPitch() : getMaxVolume();
            float min = isPitch ? getMinPitch() : getMinVolume();
            return Math.max(Math.min((max - min) * ((float) random.nextDouble()) + min, 2.0F), 0F);
        }

        @Override
        public String toString() {
            return name().replace("_", "-").toLowerCase(Locale.ROOT);
        }
    }

    public static int getMaxQuantity() {
        return get().getInt("gui.max-quantity", 640);
    }

    public static boolean isAutoStoreEnabled() {
        return get().getBoolean("auto-store.enabled", true);
    }

    public static boolean getDefaultAutoStoreValue() {
        return get().getBoolean("auto-store.default", false);
    }

    public static class BoxStick {
        public static boolean isEnabledBlockPlace() {
            return Config.get().getBoolean("box-stick.enabled.block-place", true);
        }

        public static boolean isEnabledFood() {
            return Config.get().getBoolean("box-stick.enabled.food", true);
        }

        public static boolean isEnabledPotion() {
            return Config.get().getBoolean("box-stick.enabled.potion", true);
        }

        public static boolean isEnabledTool() {
            return Config.get().getBoolean("box-stick.enabled.tool", true);
        }

        public static ItemStack get() {
            String displayName = ChatColor.translateAlternateColorCodes('&',
                    Config.get().getString("box-stick.item.display-name", "&9Box Stick"));
            List<String> lore = Config.get().getStringList("box-stick.item.lore");
            ItemStack item = new ItemStack(Material.STICK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;   
        }
    }

    public static List<World> getDisabledWorlds() {
        return get().getStringList("disabled-worlds").stream().map(Bukkit::getWorld).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<World> getAutoReplantWorlds() {
        return get().getStringList("auto-replant-worlds").stream().map(Bukkit::getWorld).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Reload config. If this method used before {@code JailConfig.save()}, the data
     * on memory will be lost.
     */
    public static void reload() {
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory);
        saveDefault();
        plugin.reloadConfig();
        config = plugin.getConfig();
        PageFunctionItems.reload();
    }

    /**
     * Saves data on memory to yaml.
     */
    public static void save() {
        plugin.saveConfig();
    }

    /**
     * Copies yaml from jar to data folder.
     */
    public static void saveDefault() {
        plugin.saveDefaultConfig();
    }

    static FileConfiguration get() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public static void reloadAllConfigs() {
        reload();
        Messages.reload();
        Prices.reload();
    }
}