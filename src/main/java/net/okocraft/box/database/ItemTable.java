/*
アイテムテーブル
id | item
1  | oaiejf=-9oqeirjo93r098h239...
2  | ...
...
*/
package net.okocraft.box.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Base64.Encoder;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.Box;
import net.okocraft.box.BoxAPI;

/**
 * このクラスで扱うアイテムのIDは、プレイヤーのデータを保存しているテーブルのcolumnとなる。このクラスはメタを持たない単純なアイテムは常に全てのテーブルにあることを保証する。
 */
final class ItemTable {

    static final String TABLE = "box_items";

    private final Database database;

    /**
     * データベース内容のキャッシュ。ここからアイテムが見つからなければデータベースに検索をかける。
     * データベースにだけ存在するアイテムの場合はこのキャッシュにアイテムを追加する。
     */
    private final BiMap<Integer, ItemStack> items = HashBiMap.create();
    private final BiMap<Integer, String> customNames = HashBiMap.create();

    ItemTable(@NotNull Database database) {
        this.database = database;
        database.execute("CREATE TABLE IF NOT EXISTS " + TABLE + " (id INTEGER PRIMARY KEY " + (database.isSQLite() ? "AUTOINCREMENT" : "AUTO_INCREMENT") + ", item VARCHAR(4096) NOT NULL, customname VARCHAR(255) UNIQUE)");
        loadItems();
        updateItems();
        addDefaultItems();
    }

    /**
     * データベースから全てのアイテムを読み込み、復号化し、マップに登録する。
     */
    private void loadItems() {
        database.query("SELECT id, item, customname FROM " + TABLE, rs -> {
            try {
                while (rs.next()) {
                    ItemStack item = fromString(rs.getString("item"));
                    item.setAmount(1);
                    int id = rs.getInt("id");
                    items.forcePut(id, item);
                    String customName = rs.getString("customname");
                    if (customName != null) {
                        customNames.put(id, customName);
                    }
                }
            } catch (SQLException ignored) {
            }
            // 何も返さない
            return null;
        });
    }

    /**
     * メタを今風にアプデする。
     */
    private void updateItems() {
        Map<Integer, String> itemStrings = toStringAll();
        if (itemStrings.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder("UPDATE " + TABLE + " SET item = CASE id");
        StringBuilder where = new StringBuilder();
        itemStrings.forEach((id, item) -> {
            sb.append(" WHEN ").append(id).append(" THEN ").append("'").append(item).append("'");
            where.append(id).append(", ");
        });
        where.delete(where.length() - 3, where.length());
        sb.append(" END WHERE id IN (").append(where).append(")");

        database.execute(sb.toString());
    }

    /**
     * メタを持たない単純なアイテムを全てデータベースに登録しておく。
     * ただし、ポーション・エンチャントに関してはクリエイティブのインベントリにあるだけ追加する。
     */
    @SuppressWarnings("deprecation")
    private void addDefaultItems() {
        List<ItemStack> defaultItems = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isLegacy() || material == Material.AIR) {
                continue;
            }

            ItemStack add = new ItemStack(material);
            if (!items.containsValue(add)) {
                defaultItems.add(add);
            }

            if (add.getItemMeta() instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta) add.getItemMeta();
                for (PotionType type : PotionType.values()) {
                    PotionMeta clonedMeta = meta.clone();
                    clonedMeta.setBasePotionData(new PotionData(type, false, false));
                    ItemStack clone = add.clone();
                    clone.setItemMeta(clonedMeta);
                    if (!items.containsValue(clone)) {
                        defaultItems.add(clone);
                    }

                    if (type.isExtendable()) {
                        clonedMeta.setBasePotionData(new PotionData(type, true, false));
                        clone = add.clone();
                        clone.setItemMeta(clonedMeta);
                        
                        if (!items.containsValue(clone)) {
                            defaultItems.add(clone);
                        }
                    }

                    if (type.isUpgradeable()) {
                        clonedMeta.setBasePotionData(new PotionData(type, false, true));
                        clone = add.clone();
                        clone.setItemMeta(clonedMeta);
                        if (!items.containsValue(clone)) {
                            defaultItems.add(clone);
                        }
                    }
                }
            }

            if (add.getItemMeta() instanceof EnchantmentStorageMeta) {
                for (Enchantment enchant : Enchantment.values()) {
                    ItemStack enchantedBook = add.clone();
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();
                    meta.addStoredEnchant(enchant, enchant.getMaxLevel(), false);
                    enchantedBook.setItemMeta(meta);
                    if (!items.containsValue(enchantedBook)) {
                        defaultItems.add(enchantedBook);
                    }
                }
            }
        }

        unSafeRegister(defaultItems);
    }

    /**
     * データベースに既存のデータが有るかの確認なしに引数のアイテムをすべて登録する。初めて初期化する時限定で使用する。
     * 
     * @param items 登録するアイテムのリスト
     */
    private void unSafeRegister(List<ItemStack> registeredItems) {
        if (registeredItems.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (ItemStack item : registeredItems) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            
            item = item.clone();
            item.setAmount(1);
            
            String itemCode = toString(item);
            if (itemCode.length() > 4096) {
                try {
                    throw new IllegalArgumentException("Too long item length (more than 4096): " + itemCode);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                continue;
            }

            sb.append("('").append(itemCode).append("'), ");
        }
        sb.setLength(sb.length() - 2);

        database.execute("REPLACE INTO " + TABLE + " (item) VALUES " + sb.toString());
        loadItems();
    }

    boolean setCustomName(ItemStack item, String customName) {
        item = item.clone();
        item.setAmount(1);

        int id = getId(item);
        if (id == -1) {
            return false;
        }

        if (customNames.containsValue(customName)) {
            return false;
        }
        customNames.put(id, customName);
        database.execute("UPDATE " + TABLE + " SET customname = '" + customName + "' WHERE id = " + id);
        return true;
    }

    String getCustomName(ItemStack item) {
        return customNames.get(getId(item));
    }

    /**
     * 新しいアイテムをデータベースに登録する。その時AUTO_INCREMENT属性によって付与されたIDを返す。
     * " + TABLE + "テーブル以外にも、autostoreとstock関係のテーブルにもカラムを追加する。
     * 
     * @param item 登録するアイテム
     * @return 新たに生成されたアイテムのID。登録済みならそのアイテムのID。登録に失敗したら-1
     */
    int register(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return -1;
        }
        item = item.clone();
        item.setAmount(1);
        int id = getId(item);
        if (id != -1) {
            return id;
        }

        String itemCode = toString(item);
        if (itemCode.length() > 4096) {
            return -1;
        }

        String sql = "INSERT INTO " + TABLE + " (item) VALUES ('" + itemCode + "')";
        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql,
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            id = rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            System.err.println("Error occurred on executing SQL: " + sql);
            e.printStackTrace();
            id = -1;
        }

        if (id != -1) {
            items.forcePut(id, item);
            BoxAPI api = Box.getInstance().getAPI();
            // on instantiate this plugin, api is null.
            if (api != null) {
                api.getItemData().loadName(item);
            }
        }

        return id;
    }

    /**
     * すでに登録済みのアイテムのIDを返す。登録していなければ-1を返す。
     * 
     * @param item 検索するアイテム
     * @return アイテムのIDか、-1
     */
    int getId(ItemStack item) {
        if (item == null) {
            return -1;
        }
        item = item.clone();
        item.setAmount(1);
        int id = items.inverse().getOrDefault(item, -1);
        if (id != -1) {
            return id;
        }

        String itemData = toString(item);
        return database.query("SELECT id FROM " + TABLE + " WHERE item = '" + itemData + "'", rs -> {
            try {
                return rs.next() ? rs.getInt("id") : -1;
            } catch (SQLException e) {
                return -1;
            }
        });
    }

    /**
     * IDからアイテムを取得する。
     * 
     * @param id
     * @return
     */
    ItemStack getItem(int id) {
        ItemStack item = items.get(id);
        if (item != null) {
            return item.clone();
        }

        return database.query("SELECT id, item FROM " + TABLE + " WHERE id = " + id, rs -> {
            try {
                if (rs.next()) {
                    ItemStack result = fromString(rs.getString("item"));
                    result.setAmount(1);
                    items.forcePut(rs.getInt("id"), item);
                    rs.updateString("item", toString(item));
                    return result.clone();
                }
            } catch (SQLException ignored) {
            }
            return null;
        });
    }

    /**
     * 登録されたアイテムのIDの全てを取得する。
     * 
     * @return
     */
    Set<Integer> getAllId() {
        return new HashSet<>(items.keySet());
    }

    /**
     * 登録されたアイテムの全てをクローンとして取得する。
     * 
     * @return
     */
    Set<ItemStack> getAllItem() {
        return items.values().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getItemMeta() != null)
                .map(ItemStack::clone).collect(Collectors.toSet());
    }

    /**
     * {@code toString(item)} によって文字列にされたアイテムを復号する。
     * 
     * @param base64 アイテムを変換した文字列
     * @return 複合されたアイテム
     */
    private ItemStack fromString(String base64) {
        try {
            String data = new String(Base64.getDecoder().decode(base64));
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(data);
            return yaml.getItemStack("i");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * アイテムを文字列に変換する。{@code fromString(base64)} メソッドで復号する。
     * 
     * @param item 文字列に返還するアイテム
     * @return アイテムを変換した文字列
     */
    private String toString(ItemStack item) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("i", item);
        return Base64.getEncoder().encodeToString(yaml.saveToString().getBytes());
    }

    private Map<Integer, String> toStringAll() {
        Encoder encoder = Base64.getEncoder();
        YamlConfiguration yaml = new YamlConfiguration();
        Map<Integer, String> result = new HashMap<>();
        items.forEach((id, item) -> {
            yaml.set("i", item);
            result.put(id, encoder.encodeToString(yaml.saveToString().getBytes()));
        });
        return result;
    }
}