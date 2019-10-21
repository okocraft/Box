package net.okocraft.box.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.box.Box;
import net.okocraft.box.database.Items;

public final class Categories extends CustomConfig {

    private static Box plugin = Box.getInstance();
    private static final NamespacedKey CATEGORY_NAME_KEY = new NamespacedKey(plugin, "categoryname");
    private static final Categories INSTANCE = new Categories("categories.yml");

    private Map<String, Category> categoryCache = new HashMap<>();

    public class Category {

        private final String name;
        private final String displayName;
        private final ItemStack icon;
        private final List<String> items;
    
        public Category(String name, String displayName, ItemStack icon, List<String> items) {
            this.name = name;
            this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
            this.icon = icon;
            this.items = items;
            this.items.removeIf(itemName -> !Items.contains(itemName));
    
            if (icon.getType() != Material.AIR) {
                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(this.displayName);
                meta.getPersistentDataContainer().set(CATEGORY_NAME_KEY, PersistentDataType.STRING, this.name);
                icon.setItemMeta(meta);
            }
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<String> getItems() {
            return Collections.unmodifiableList(items);
        }

        public ItemStack getIcon() {
            return icon.clone();
        }
    }

    private Categories(String name) {
        super(name);
    }

    public static Categories getInstance() {
        return INSTANCE;
    }

    public String getDisplayName(String categoryName) throws IllegalArgumentException {
        Category category = getCategory(categoryName);
        return category.getName();
    }

    public ItemStack getIcon(String categoryName) throws IllegalArgumentException {
        Category category = getCategory(categoryName);
        return category.getIcon();
    }
    
    public List<String> getItems(String categoryName) throws IllegalArgumentException {
        Category category = getCategory(categoryName);
        return category.getItems();
    }

    public Category addCategory(String id, String displayName, List<String> items, String iconItem) throws IllegalArgumentException {
        displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        items.removeIf(itemName -> !Items.contains(itemName));
        if (!Items.contains(iconItem)) {
            throw new IllegalArgumentException("The item" + iconItem + " is not registered");
        }
        get().createSection(id);
        get().set(id + ".display-name", displayName);
        get().set(id + ".icon", iconItem);
        get().set(id + ".item", items);
        save();
        return getCategory(id);
    }

    public Category getCategory(String categoryName) throws IllegalArgumentException {
        if (categoryCache.containsKey(categoryName)) {
            return categoryCache.get(categoryName);
        }
        if (!get().contains(categoryName)) {
            throw new IllegalArgumentException("The category \"" + categoryName + "\" does not exist.");
        }
        List<String> items = get().getStringList(categoryName + ".item");
        String displayName = Config.getCategorySelectionConfig().getItemNameFormat()
                .replaceAll("%category-name%", categoryName)
                .replaceAll("%display-name%", get().getString(categoryName + ".display-name", categoryName + ".display-name"));
        ItemStack icon = Items.getItemStack(get().getString(categoryName + ".icon").toUpperCase(Locale.ROOT));
        Category result = INSTANCE.new Category(categoryName, displayName, icon, items);
        categoryCache.put(categoryName, result);
        return result;
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        get().getValues(false).keySet()
                .forEach(categoryName -> categories.add(getCategory(categoryName)));
        return categories;
    }

    public boolean exist(String categoryName) {
        return get().contains(categoryName);
    }

    public List<String> getAllItems() {
        return getAllCategories().stream().flatMap(category -> category.getItems().stream()).distinct().collect(Collectors.toList());
    }

    /**
     * Reload config. If this method used before {@code JailConfig.save()}, the
     * data on memory will be lost.
     */
    @Override
    public void reload() {
        super.reload();
        if (INSTANCE != null) {
            // INSTANCE が null になるのは初期化時のみ
            categoryCache.clear();
        }
    }
}