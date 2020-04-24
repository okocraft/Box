package net.okocraft.box.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.okocraft.box.Box;
import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Config;
import net.okocraft.box.config.Layouts;
import net.okocraft.box.config.Messages;
import net.okocraft.box.database.ItemData;
import net.okocraft.box.database.PlayerData;

/**
 * ページ切り替えメソッドや、サブクラスで拡張すべきプレースホルダ置換メソッドを搭載したGUI基底クラス。
 */
public abstract class BaseGUI implements InventoryHolder {

    /** プラグイン */
    protected Box plugin = Box.getInstance();

    /** 設定管理クラス */
    protected Config config = plugin.getAPI().getConfig();
    
    /** メッセージ管理クラス */
    protected Messages messages = plugin.getAPI().getMessages();
    
    /** カテゴリ管理クラス */
    protected Categories categories = plugin.getAPI().getCategories();
    
    /** GUIアイコン管理クラス */
    protected Layouts layout = plugin.getAPI().getLayouts();

    /** プレイヤーデータ管理クラス */
    protected PlayerData playerData = plugin.getAPI().getPlayerData();

    /** アイテム名取得クラス */
    protected ItemData itemData = plugin.getAPI().getItemData();
    
    /** GUIの実体 */
    private final Inventory GUI;

    /** ページ移動アイテム以外の枠に順番に入っていくアイテムのリスト */
    private final List<ItemStack> items = new ArrayList<>();

    /** 装飾のための枠や、ページ移動アイテムなど */
    private final Map<Integer, ItemStack> pageCommonItems = new HashMap<>();

    /**
     * {@code items} や {@code pageCommonItems} などのアイテムをスロット順かつページ順に並び替えたもの。
     * このリストの要素は {@code items} や {@code pageCommonItems} の要素のクローンで構成される。 
     */
    private List<ItemStack> pagedItems = new ArrayList<>();

    /** GUIのページ */
    private int page = 1;

    /** アイテムが存在する最大のページ */
    private int maxPage = 1;

    BaseGUI(int GUISize, String title) {
        if (GUISize < 0 || GUISize > 54) {
            GUISize = 54;
        }
        if (GUISize % 9 != 0) {
            GUISize = (GUISize / 9) * 9;
        }

        this.GUI = Bukkit.createInventory(this, GUISize, Objects.requireNonNullElse(title, ""));
    }

    int getPage() {
        return page;
    }

    int getMaxPage() {
        return maxPage;
    }

    /**
     * ページ共通アイテムのマップを不変にしたものを返す。
     * 
     * @return 不変な共通アイテムのマップ
     */
    Map<Integer, ItemStack> getPageCommonItems() {
        return Collections.unmodifiableMap(pageCommonItems);
    }

    /**
     * ページ共通ではないアイテムのリストの不変にしたものを返す。
     * 
     * @return 不変な非共通アイテムのリスト
     */
    List<ItemStack> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * このGUIの特定のスロットにどのページでも共通なアイテムを追加する。
     * 
     * @param slot アイテムを追加するスロット
     * @param item 追加するアイテム
     */
    void putPageCommonItem(int slot, ItemStack item) {
        pageCommonItems.put(slot, item);
        pagedItems = paged();
    }

    /**
     * このGUIの特定のスロットに、どのページでも共通なアイテムを追加する。
     * 単体用メソッドを複数回呼び出すよりも軽量に動作するが
     * 一つだけ追加する場合は単体用メソッドのほうがパフォーマンスに優れる。
     * 
     * @param entries スロットとアイテムのマップ
     */
    void putPageCommonItems(Map<Integer, ItemStack> entries) {
        pageCommonItems.putAll(entries);
        pagedItems = paged();
    }

    /**
     * このGUIの特定のスロットから、どのページでも共通なアイテムを消す。
     * 
     * @param slot 共通なアイテムが有るスロット、なければ何もしない
     */
    void removePageCommonItem(int slot) {
        if (pageCommonItems.remove(slot) != null) {
            pagedItems = paged();
        }
    }

    /**
     * このGUIの特定のスロット群から、どのページでも共通なアイテムを消す。
     * 単体用メソッドを複数回呼び出すよりも軽量に動作するが
     * 一つだけ追加する場合は単体用メソッドのほうがパフォーマンスに優れる。
     * 
     * @param slots 共通のアイテムがあるスロットの集合
     */
    void removePageCommmonItems(Collection<Integer> slots) {
        boolean modified = false;
        for (Integer slot : slots) {
            if (pageCommonItems.remove(slot) != null) {
                modified = true;
            }
        }

        if (modified) {
            pagedItems = paged();
        }
    }

    /**
     * このGUIのページ共通なアイテムのうち、指定したitemと同じものをすべて消す。
     * 
     * @param item 消すアイテム
     */
    void removePageCommonItem(ItemStack item) {
        boolean modified = false;
        for (Map.Entry<Integer, ItemStack> entry : new HashMap<>(pageCommonItems).entrySet()) {
            if (item.equals(entry.getValue())) {
                ItemStack removed = pageCommonItems.remove(entry.getKey());
                if ((removed == null && entry.getKey() != null) || removed.equals(entry.getValue())) {
                    modified = true;
                }
            }
        }
        if (modified) {
            pagedItems = paged();
        }
    }

    /**
     * ページ共通ではないアイテムに、指定したアイテムを追加する。
     * 
     * @param item 追加するアイテム
     */
    void addItem(ItemStack item) {
        if (items.add(item)) {
            pagedItems = paged();
        }
    }

    /**
     * ページ共通ではないアイテムに、指定したアイテムをすべて追加する。
     * 単体用メソッドを複数回呼び出すよりも軽量に動作するが
     * 一つだけ追加する場合は単体用メソッドのほうがパフォーマンスに優れる。
     * 
     * @param itemCollection 追加するアイテムの集合
     */
    void addAllItem(Collection<ItemStack> itemCollection) {
        if (items.addAll(itemCollection)) {
            pagedItems = paged();
        }
    }

    /**
     * ページ共通ではないアイテムに、指定したアイテムが含まれていれば消す。
     * 
     * @param item 消すアイテム
     */
    void removeItem(ItemStack item) {
        if (items.remove(item)) {
            pagedItems = paged();
        }
    }
    
    
    /**
     * ページ共通ではないアイテムに、指定したアイテムが含まれていれば消す。
     * 単体用メソッドを複数回呼び出すよりも軽量に動作するが
     * 一つだけ追加する場合は単体用メソッドのほうがパフォーマンスに優れる。
     * 
     * @param itemCollection 消すアイテムの集合
     */
    void removeAllItem(Collection<ItemStack> itemCollection) {
        if (items.removeAll(itemCollection)) {
            pagedItems = paged();
        }
    }

    void clearItems() {
        items.clear();
        pagedItems = paged();
    }
    
    /**
     * GUIのアイテムをこのページのアイテムでアップデートする。
     * 
     * @param page 目的のページ
     */
    void setPage(int page) {
        this.page = Math.min(maxPage, Math.max(1, page));
        List<ItemStack> pageItems = getPageItems(this.page);
        GUI.setContents(pageItems.toArray(new ItemStack[pageItems.size()]));
    }

    /**
     * アイテムの名前と説明文にプレースホルダを適応する。引数のアイテムスタックにそのまま新しい名前と説明文を適応しているため、もともとのアイテムスタックを保持したい場合はクローンしてから引数に渡すこと。
     * 
     * @param item プレースホルダを適応するアイテム
     * @param placeholder 適応するプレースホルダ。サブクラスで中身を増やすことがあるため、{@code Map.of()} などの容量不変のマップを引数に渡さないこと。
     * @return 適応後のアイテム
     */
    ItemStack applyPlaceholder(ItemStack item, Map<String, String> placeholder) {
        if (item == null || item.getItemMeta() == null || placeholder.isEmpty()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        String name = meta.getDisplayName();

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        for (Map.Entry<String, String> entry : placeholder.entrySet()) {
            name = name.replaceAll(entry.getKey(), entry.getValue());
            if (!lore.isEmpty()) {
                lore.replaceAll(loreLine -> loreLine.replaceAll(entry.getKey(), entry.getValue()));
            }
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        lore.replaceAll(loreLine -> ChatColor.translateAlternateColorCodes('&', loreLine));
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    ItemStack applyPlaceholder(ItemStack item) {
        return applyPlaceholder(item, new HashMap<>());
    }

    /**
     * ページ共通アイテムとページ共通でないアイテムを、各ページのスロット番号通りに並べる。1ページ目以降もそのページのスロット通りに並べられる。
     * 
     * @return 各ページのスロット番号通りに並べられたアイテムのリスト
     */
    private List<ItemStack> paged() {
        List<ItemStack> result = new ArrayList<>();

        Iterator<ItemStack> itemItr = items.iterator();
        int page = 0;

        do {
            page++;
            for (int slot = 0; slot < GUI.getSize(); slot++) {
                if (pageCommonItems.containsKey(slot)) {
                    result.add(pageCommonItems.get(slot));
                } else if (itemItr.hasNext()) {
                    result.add(itemItr.next());
                } else {
                    result.add(null);
                }
            }
        } while (itemItr.hasNext());

        this.maxPage = page;

        return result;
    }

    /**
     * 指定したページを構成するアイテムをすべてリストに収めて返す。インデックスとスロットが対応する。このリストのアイテムはプレースホルダが置換されておらず、アイテムスタックのインスタンスはaddまたはputしたままのもの。
     * 
     * @param page ページ
     * @return スロット番号通りに並べられたアイテムリスト
     */
    List<ItemStack> getRawPageItems(int page) {
        // subListメソッドにおけるIndexOutOfBoundExceptionの条件を網羅する。
        if (page > maxPage) {
            page = maxPage;
        } else if (page < 1) {
            page = 1;
        }

        return new ArrayList<>(pagedItems.subList(GUI.getSize() * (page - 1), GUI.getSize() * page));
    }

    /**
     * 指定したページを構成するアイテムをすべてリストに収めて返す。インデックスとスロットが対応する。このリストのアイテムはクローンである、かつプレースホルダを置換してある。
     * 
     * @param page ページ
     * @return スロット番号通りに並べられたアイテムリスト
     */
    List<ItemStack> getPageItems(int page) {
        List<ItemStack> result = getRawPageItems(page);
        result.replaceAll(item -> item != null ? applyPlaceholder(item) : null);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory getInventory() {
        return GUI;
    }
}