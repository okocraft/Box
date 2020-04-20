package net.okocraft.box.config;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.box.Box;

public class Layouts extends CustomConfig {

    private final NamespacedKey realItemKey = new NamespacedKey(Box.getInstance(), "realitem");

    public Layouts() {
        super("layout.yml");
    }

    /**
     * @return the frame icon
     */
    public ItemStack getFrame() {
        ItemStack frame = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        setIconMeta(frame, "frame");
        return frame;
    }

    /**
     * @return the previousPage icon
     */
    public ItemStack getPreviousPage() {
        ItemStack previousPage = new ItemStack(Material.ARROW);
        setIconMeta(previousPage, "previous-page");
        return previousPage;
    }

    /**
     * @return the nextPage icon
     */
    public ItemStack getNextPage() {
        ItemStack nextPage = new ItemStack(Material.ARROW);
        setIconMeta(nextPage, "next-page");
        return nextPage;
    }

    /**
     * @return the backMenu icon
     */
    public ItemStack getBackMenu() {
        ItemStack backMenu = new ItemStack(Material.OAK_DOOR);
        setIconMeta(backMenu, "back-menu");
        return backMenu;
    }

    /**
     * @return the decrease icon
     */
    public ItemStack getDecrease() {
        ItemStack decrease = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        setIconMeta(decrease, "decrease");
        return decrease;
    }

    /**
     * @return the changeUnit icon
     */
    public ItemStack getChangeUnit() {
        ItemStack changeUnit = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        setIconMeta(changeUnit, "change-unit");
        return changeUnit;
    }

    /**
     * @return the increase icon
     */
    public ItemStack getIncrease() {
        ItemStack increase = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        setIconMeta(increase, "increase");
        return increase;
    }

    /**
     * @return the strage icon
     */
    public ItemStack getStrage() {
        ItemStack strage = new ItemStack(Material.CHEST);
        setIconMeta(strage, "strage");
        return strage;
    }

    /**
     * @return the shop icon
     */
    public ItemStack getShop() {
        ItemStack shop = new ItemStack(Material.GOLD_NUGGET);
        setIconMeta(shop, "shop");
        return shop;
    }

    /**
     * @return the craft icon
     */
    public ItemStack getCraft() {
        ItemStack craft = new ItemStack(Material.CRAFTING_TABLE);
        setIconMeta(craft, "craft");
        return craft;
    }

    /**
     * @return the selectOffline icon
     */
    public ItemStack getSelectOffline() {
        ItemStack selectOffline = new ItemStack(Material.SKELETON_SKULL);
        setIconMeta(selectOffline, "select-offline");
        return selectOffline;
    }

    /**
     * @return the selectOnline icon
     */
    public ItemStack getSelectOnline() {
        ItemStack selectOnline = new ItemStack(Material.PLAYER_HEAD);
        setIconMeta(selectOnline, "select-online");
        return selectOnline;
    }

    public String getCategorySelectorGUITitle() {
        return get().getString("category-selector-gui.title", "カテゴリー選択");
    }

    public ItemStack setCategorySelectorEntryMeta(ItemStack entry) {
        setIconMeta(entry, "category-selector-gui.item-format");
        return entry;
    }

    private ItemStack setEntryMeta(ItemStack entry, String iconKey) {
        String realItemName = Box.getInstance().getAPI().getItemData().getName(entry);
        if (realItemName == null) {
            realItemName = entry.getItemMeta().getPersistentDataContainer().get(realItemKey, PersistentDataType.STRING);
        }
        setIconMeta(entry, iconKey);
        if (realItemName != null) {
            setRealItemTag(entry, realItemName);
        }
        return entry;
    }

    public ItemStack setStrageEntryMeta(ItemStack entry) {
        return setEntryMeta(entry, "gui-entry.strage");
    }
    
    public ItemStack setShopEntryMeta(ItemStack entry) {
        return setEntryMeta(entry, "gui-entry.shop");
    }

    public ItemStack setCraftEntryMeta(ItemStack entry) {
        return setEntryMeta(entry, "gui-entry.craft");
    }

    public String getStrageGUITitle() {
        return get().getString("strage-gui-title", "%category-name%");
    }

    public String getShopGUITitle() {
        return get().getString("shop-gui-title", "%category-name%");
    }

    public String getCraftGUITitle() {
        return get().getString("craft-gui-title", "%category-name%");
    }

    public String getMaterialsPlaceholderFormat() {
        return get().getString("gui-entry.craft.materials-placeholder-format", "&7  %material%: %material-stock% / %amount%");
    }

    private ItemStack setIconMeta(ItemStack icon, String iconKey) {
        if (icon == null || iconKey == null || icon.getItemMeta() == null) {
            return icon;
        }
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(getDisplayName(iconKey));
        meta.setLore(getLore(iconKey));
        icon.setItemMeta(meta);
        return icon; 
    }

    private ItemStack setRealItemTag(ItemStack icon, String realItemName) {
        if (icon == null || icon.getItemMeta() == null) {
            return icon;
        }
        ItemMeta meta = icon.getItemMeta();
        meta.getPersistentDataContainer().set(realItemKey, PersistentDataType.STRING, realItemName);
        icon.setItemMeta(meta);
        return icon; 
    }

    private String getDisplayName(String iconKey) {
        String key = iconKey + ".display-name";
        return get().getString(key, key);
    }
    
    private List<String> getLore(String iconKey) {
        String key = iconKey + ".lore";
        List<String> lore = get().getStringList(key);
        return lore;
    }
}
