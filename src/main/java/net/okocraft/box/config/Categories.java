package net.okocraft.box.config;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.box.Box;
import net.okocraft.box.database.ItemData;

public class Categories extends CustomConfig {

    private final Box plugin = Box.getInstance();
    private final ItemData itemData = plugin.getAPI().getItemData();

    private final NamespacedKey categoryNameKey = new NamespacedKey(plugin, "categoryname");

    private Set<String> allItems;

    public Categories() {
        super("categories.yml");
    }

    public List<String> getCategories() {
        return get().getKeys(false).stream()
                .filter(category -> Objects.nonNull(getDisplayName(category)))
                .filter(category -> Objects.nonNull(getIcon(category)))
                .filter(category -> !getItems(category).isEmpty())
                .collect(Collectors.toList());
    }

    public String getDisplayName(String category) {
        return get().getString(category + ".display-name");
    }

    public ItemStack getIcon(String category) {
        ItemStack item = itemData.getItemStack(get().getString(category + ".icon", ""));
        if (item == null) {
            return item;
        }
        item = plugin.getAPI().getLayouts().setCategorySelectorEntryMeta(item);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(categoryNameKey, PersistentDataType.STRING, category);
        item.setItemMeta(meta);
        return item;
    }

    public List<ItemStack> getItems(String category) {
        return get().getStringList(category + ".item").stream()
                .map(itemData::getItemStack)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Set<String> getAllItems() {
        if (allItems != null) {
            new BukkitRunnable(){
            
                @Override
                public void run() {
                    Set<String> items = getCategories().stream().flatMap(category -> get().getStringList(category + ".item").stream()).collect(Collectors.toSet());
                    if (allItems.containsAll(items)) {
                        allItems = items;
                    }
                }
            }.runTaskAsynchronously(plugin);

            return allItems;
        }

        allItems = getCategories().stream().flatMap(category -> get().getStringList(category + ".item").stream()).collect(Collectors.toSet());
        return allItems;
    }
    
    public void addCategory(String id, String displayName, List<String> items, String iconItem) throws IllegalArgumentException {
        displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        // TODO: register unexisting items on addcategory.
        // items.removeIf(itemName -> !itemData.contains(itemName));
        // if (!Items.contains(iconItem)) {
        //     throw new IllegalArgumentException("The item" + iconItem + " is not registered");
        // }
        get().createSection(id);
        get().set(id + ".display-name", displayName);
        get().set(id + ".icon", iconItem);
        get().set(id + ".item", items);
        save();
    }
}
