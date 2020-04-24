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

    /**
     * コンストラクタ。
     * @deprecated 内部利用限定。このコンストラクタを使わず、{@code Box.getInstance().getAPI().getLayouts()}を使用すること。
     */
    @Deprecated
    public Layouts() {
        super("layout.yml");
    }

    /**
     * カテゴリセレクターの枠を取得する。
     * 
     * @return フレーム
     */
    public ItemStack getFrame() {
        ItemStack frame = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        setIconMeta(frame, "frame");
        return frame;
    }

    /**
     * 前ページに移動するボタンを取得する。
     * 
     * @return 前ページボタン
     */
    public ItemStack getPreviousPage() {
        ItemStack previousPage = new ItemStack(Material.ARROW);
        setIconMeta(previousPage, "previous-page");
        return previousPage;
    }

    /**
     * 次ページに移動するボタンを取得する。
     * 
     * @return 次ページボタン
     */
    public ItemStack getNextPage() {
        ItemStack nextPage = new ItemStack(Material.ARROW);
        setIconMeta(nextPage, "next-page");
        return nextPage;
    }

    /**
     * 前のメニューに戻るボタンを取得する。
     * 
     * @return 前のメニューに戻るボタン
     */
    public ItemStack getBackMenu() {
        ItemStack backMenu = new ItemStack(Material.OAK_DOOR);
        setIconMeta(backMenu, "back-menu");
        return backMenu;
    }

    /**
     * 取引数減少ボタンを取得する。
     * 
     * @return 取引数減少ボタン
     */
    public ItemStack getDecrease() {
        ItemStack decrease = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        setIconMeta(decrease, "decrease");
        return decrease;
    }

    /**
     * 取引数変更量を設定するボタンを取得する。
     * このアイテムの量によって、一度の取引数減少ボタン・取引数増加ボタンで変更される取引数が増減する。
     * 
     * @return 取引数変更量設定ボタン
     */
    public ItemStack getChangeUnit() {
        ItemStack changeUnit = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        setIconMeta(changeUnit, "change-unit");
        return changeUnit;
    }

    /**
     * 取引数増加ボタンを取得する。
     * 
     * @return 取引数増加ボタン
     */
    public ItemStack getIncrease() {
        ItemStack increase = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        setIconMeta(increase, "increase");
        return increase;
    }

    /**
     * アイテムの預け入れ、引き出しを行うGUIに移動するボタンを取得する。
     * 
     * @return GUI移動ボタン
     */
    public ItemStack getStrage() {
        ItemStack strage = new ItemStack(Material.CHEST);
        setIconMeta(strage, "strage");
        return strage;
    }

    /**
     * アイテムの購入、売却を行うGUIに移動するボタンを取得する。
     * 
     * @return GUI移動ボタン
     */
    public ItemStack getShop() {
        ItemStack shop = new ItemStack(Material.GOLD_NUGGET);
        setIconMeta(shop, "shop");
        return shop;
    }

    /**
     * アイテムのクラフトを行うGUIに移動するボタンを取得する。
     * 
     * @return GUI移動ボタン
     */
    public ItemStack getCraft() {
        ItemStack craft = new ItemStack(Material.CRAFTING_TABLE);
        setIconMeta(craft, "craft");
        return craft;
    }

    /**
     * オフラインプレイヤーを表示するボタンを取得する。
     * 
     * @return オフラインプレイヤーを表示するボタン
     * 
     * @deprecated 未使用。
     */
    @Deprecated
    public ItemStack getSelectOffline() {
        ItemStack selectOffline = new ItemStack(Material.SKELETON_SKULL);
        setIconMeta(selectOffline, "select-offline");
        return selectOffline;
    }

    /**
     * オフラインプレイヤーを表示するボタンを取得する。
     * 
     * 
     * @return オフラインプレイヤーを表示するボタン
     * 
     * @deprecated 未使用。
     */
    @Deprecated
    public ItemStack getSelectOnline() {
        ItemStack selectOnline = new ItemStack(Material.PLAYER_HEAD);
        setIconMeta(selectOnline, "select-online");
        return selectOnline;
    }

    /**
     * カテゴリセレクターGUIのタイトルを取得する。
     * 
     * @return タイトル
     */
    public String getCategorySelectorGUITitle() {
        return get().getString("category-selector-gui.title", "カテゴリー選択");
    }

    /**
     * カテゴリセレクターGUIに並べられるカテゴリアイテムのメタを渡された{@code entry}にセットする。
     * 
     * @param entry メタをセットするアイテム
     * @return メタをセットされたアイテム。同じインスタンス
     */
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

    /**
     * アイテムの預け入れ、引き出しを行うGUIに並べられるアイテムのメタを渡された{@code entry}にセットする。
     * 
     * @param entry メタをセットするアイテム
     * @return メタをセットされたアイテム。同じインスタンス
     */
    public ItemStack setStrageEntryMeta(ItemStack entry) {
        return setEntryMeta(entry, "gui-entry.strage");
    }
    
    /**
     * アイテムの購入、売却を行うGUIに並べられるアイテムのメタを渡された{@code entry}にセットする。
     * 
     * @param entry メタをセットするアイテム
     * @return メタをセットされたアイテム。同じインスタンス
     */
    public ItemStack setShopEntryMeta(ItemStack entry) {
        return setEntryMeta(entry, "gui-entry.shop");
    }

    /**
     * アイテムのクラフトを行うGUIに並べられるカテゴリアイテムのメタを渡された{@code entry}にセットする。
     * 
     * @param entry メタをセットするアイテム
     * @return メタをセットされたアイテム。同じインスタンス
     */
    public ItemStack setCraftEntryMeta(ItemStack entry) {
        return setEntryMeta(entry, "gui-entry.craft");
    }

    /**
     * アイテムの預け入れ、引き出しを行うGUIのタイトルを取得する。
     * 
     * @return タイトル 
     */
    public String getStrageGUITitle() {
        return get().getString("strage-gui-title", "%category-name%");
    }

    /**
     * アイテムの購入、売却を行うGUIのタイトルを取得する。
     * 
     * @return タイトル
     */
    public String getShopGUITitle() {
        return get().getString("shop-gui-title", "%category-name%");
    }

    /**
     * アイテムのクラフトを行うGUIのタイトルを取得する。
     * 
     * @return タイトル
     */
    public String getCraftGUITitle() {
        return get().getString("craft-gui-title", "%category-name%");
    }

    /**
     * クラフトに使われる材料のリストの一行一行に用いられるフォーマットを取得する。
     * 
     * @return フォーマット
     */
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
