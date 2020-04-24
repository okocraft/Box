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

/**
 * GUIに実際に並び、取引可能なアイテムを取得できる。
 */
public final class Categories extends CustomConfig {

    private final Box plugin = Box.getInstance();

    private final NamespacedKey categoryNameKey = new NamespacedKey(plugin, "categoryname");

    private Set<String> allItems;

    /**
     * コンストラクタ。
     * @deprecated 内部利用限定。このコンストラクタを使わず、{@code Box.getInstance().getAPI().getConfig()}を使用すること。
     */
    @Deprecated
    public Categories() {
        super("categories.yml");
    }

    /**
     * configに設定されている全てのカテゴリを取得する。
     * 
     * @return カテゴリ名のリスト
     */
    public List<String> getCategories() {
        return get().getKeys(false).stream()
                .filter(category -> Objects.nonNull(getDisplayName(category)))
                .filter(category -> Objects.nonNull(getIcon(category)))
                .filter(category -> !getItems(category).isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * categoryの表示名を取得する。
     * 
     * @param category 表示名を取得するカテゴリ
     * @return categoryの表示名
     */
    public String getDisplayName(String category) {
        return get().getString(category + ".display-name");
    }

    /**
     * categoryのアイコンを取得する。
     * 
     * @param category アイコンを取得するカテゴリ
     * @return categoryのアイコン
     */
    public ItemStack getIcon(String category) {
        ItemStack item = plugin.getAPI().getItemData().getItemStack(get().getString(category + ".icon", ""));
        if (item == null) {
            return item;
        }
        item = plugin.getAPI().getLayouts().setCategorySelectorEntryMeta(item);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(categoryNameKey, PersistentDataType.STRING, category);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * カテゴリに設定されているすべてのアイテムを取得する。
     * 
     * @param category すべてのアイテムを取得するカテゴリ
     * @return categoryに設定されているアイテム
     */
    public List<ItemStack> getItems(String category) {
        return get().getStringList(category + ".item").stream()
                .map(plugin.getAPI().getItemData()::getItemStack)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * カテゴリ関係なくすべてのアイテムを取得する。
     * 
     * @return すべてのアイテムのセット
     */
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
    
    /**
     * カテゴリを追加する。
     * 
     * @param id カテゴリ名。configのキーとなる。
     * @param displayName カテゴリの表示名
     * @param items カテゴリのアイテムのリスト
     * @param iconItem カテゴリのアイコン
     */
    public void addCategory(String id, String displayName, List<String> items, String iconItem) {
        get().set(id + ".display-name", ChatColor.translateAlternateColorCodes('&', displayName));
        get().set(id + ".icon", iconItem);
        get().set(id + ".item", items);
        save();
    }
    
    /**
     * customnameコマンドによってアイテム名が変化したとき、追従するためのメソッド。
     * すべてのアイテムから{@code oldName}の名前を持つアイテムを検索し、そのアイテムを{@code newName}に変更する。
     * 
     * @param oldName 古いアイテム名
     * @param newName 新たなアイテム名
     */
    public void replaceItem(String oldName, String newName) {
        getCategories().forEach(category -> {
            List<String> items = get().getStringList(category + ".item");
            items.replaceAll(item -> item.replaceAll(oldName, newName));
            get().set(category + ".item", items);
        });

        save();
    }
}
