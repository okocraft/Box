package net.okocraft.box.plugin.database.table;

import net.okocraft.box.plugin.database.connector.Database;
import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ItemTable extends AbstractTable {

    private final static String ITEM_YAML_PATH = "i";

    private final static String ITEM_SELECT = "select id from %table% where item=?";
    private final static String ITEM_SELECT_ALL = "select id, item, customname from %table%";
    private final static String ITEM_INSERT
            = "insert into %table% (item) select ? where not exists (select item from %table% where item=?)";
    private final static String ITEM_UPDATE_ALL_PREFIX = "update %table% set item = case id";
    private final static String ITEM_UPDATE_CUSTOM_NAME_BY_ID = "update %table% set customname=? where id=? limit 1";

    private final YamlConfiguration yamlForEncode = new YamlConfiguration();
    private final YamlConfiguration yamlForDecode = new YamlConfiguration();

    public ItemTable(@NotNull Database database, @NotNull String prefix) {
        super(database, prefix + "items");
    }

    @NotNull
    public Set<Item> loadAllItems() throws SQLException {
        saveItems(getDefaultItems());

        Set<Item> items = new HashSet<>();

        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(ITEM_SELECT_ALL))) {
            ResultSet result = st.executeQuery();

            while (result.next()) {
                ItemStack item = fromString(result.getString("item"));

                if (item == null) {
                    continue;
                }

                items.add(new Item(result.getInt("id"), item, result.getString("customname")));
            }
        }

        return items;
    }

    public void updateItems(@NotNull Set<Item> items) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder(replaceTableName(ITEM_UPDATE_ALL_PREFIX));
        List<String> idCollector = new LinkedList<>();

        for (Item item : items) {
            // when id them 'item'
            sqlBuilder.append(" when ").append(item.getInternalID())
                    .append(" then '").append(toString(item.getOriginal())).append('\'');
            idCollector.add(String.valueOf(item.getInternalID()));
        }

        sqlBuilder.append(" end where id in (").append(String.join(", ", idCollector)).append(')');

        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(sqlBuilder.toString())) {
            st.execute();
        }
    }

    @NotNull
    public Item registerItem(@NotNull ItemStack item) throws SQLException, IllegalStateException {
        try (Connection c = database.getConnection()) {
            String strItem = toString(item);

            try (PreparedStatement st = c.prepareStatement(replaceTableName(ITEM_INSERT))) {
                st.setString(1, strItem);
                st.setString(2, strItem);
                st.execute();
            }

            try (PreparedStatement st = c.prepareStatement(replaceTableName(ITEM_SELECT))) {
                st.setString(1, strItem);

                if (4096 < strItem.length()) {
                    throw new IllegalArgumentException("Too long item length (more than 4096): " + strItem);
                }

                ResultSet result = st.executeQuery();
                if (result.next()) {
                    return new Item(result.getInt("id"), item);
                }
            }
        }

        throw new IllegalStateException("Could not register item");
    }

    public void saveCustomName(@NotNull Item item) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(ITEM_UPDATE_CUSTOM_NAME_BY_ID))) {
            st.setString(1, item.isCustomizedName() ? item.getName() : null);
            st.setInt(2, item.getInternalID());
            st.execute();
        }
    }

    /**
     * {@code toString(item)} によって文字列にされたアイテムを復号する。
     *
     * @param base64 アイテムを変換した文字列
     * @return 複合されたアイテム
     */
    @Nullable
    private ItemStack fromString(String base64) {
        if (base64 != null) {
            try {
                String data = new String(Base64.getDecoder().decode(base64));
                yamlForDecode.loadFromString(data);
                return yamlForDecode.getItemStack(ITEM_YAML_PATH);
            } catch (InvalidConfigurationException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @NotNull
    private String toString(@NotNull ItemStack item) {
        yamlForEncode.set(ITEM_YAML_PATH, item);
        return Base64.getEncoder().encodeToString(yamlForEncode.saveToString().getBytes());
    }

    @NotNull
    private List<ItemStack> getDefaultItems() {
        List<ItemStack> result = new LinkedList<>();

        for (Material material : Material.values()) {
            if (material.name().startsWith("LEGACY_") || material.isAir()) {
                continue;
            }

            ItemStack item = new ItemStack(material);
            result.add(item);

            ItemMeta meta = item.getItemMeta();

            if (meta instanceof PotionMeta) {
                addPotions(result, item, (PotionMeta) meta);
            }

            if (meta instanceof EnchantmentStorageMeta) {
                addEnchants(result, item, (EnchantmentStorageMeta) meta);
            }
        }

        return result;
    }

    private void saveItems(@NotNull List<ItemStack> items) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(ITEM_INSERT))) {

            for (ItemStack item : items) {
                String strItem = toString(item);

                if (4096 < strItem.length()) {
                    Logger.getGlobal().warning("Too long item length (more than 4096), ignore it. code:" + strItem);
                    // TODO: Plugin's logger
                    continue;
                }

                st.setString(1, strItem);
                st.setString(2, strItem);
                st.addBatch();
            }

            st.executeBatch();
        }
    }

    private void addPotions(@NotNull List<ItemStack> toCollect, @NotNull ItemStack item, @NotNull PotionMeta meta) {
        for (PotionType type : PotionType.values()) {
            ItemStack clonedItem = item.clone();
            PotionMeta clonedMeta = meta.clone();

            clonedMeta.setBasePotionData(new PotionData(type, false, false));
            clonedItem.setItemMeta(clonedMeta);

            toCollect.add(clonedItem);

            if (type.isExtendable()) {
                clonedItem = item.clone();
                clonedMeta = meta.clone();

                clonedMeta.setBasePotionData(new PotionData(type, true, false));
                clonedItem.setItemMeta(clonedMeta);

                toCollect.add(clonedItem);
            }

            if (type.isUpgradeable()) {
                clonedItem = item.clone();
                clonedMeta = meta.clone();

                clonedMeta.setBasePotionData(new PotionData(type, false, true));
                clonedItem.setItemMeta(clonedMeta);

                toCollect.add(clonedItem);
            }
        }
    }

    private void addEnchants(@NotNull List<ItemStack> toCollect,
                             @NotNull ItemStack item, @NotNull EnchantmentStorageMeta meta) {
        for (Enchantment enchant : Enchantment.values()) {
            ItemStack clonedItem = item.clone();
            EnchantmentStorageMeta clonedMeta = meta.clone();

            clonedMeta.addStoredEnchant(enchant, enchant.getMaxLevel(), false);
            clonedItem.setItemMeta(clonedMeta);

            toCollect.add(clonedItem);
        }
    }

    @Override
    protected void createTable() throws SQLException {
        try (Connection c = database.getConnection(); Statement statement = c.createStatement()) {

            statement.addBatch(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "id INTEGER PRIMARY KEY " + getAutoIncrementSQL() + ", " +
                            "item VARCHAR(4096) NOT NULL, " +
                            "customname VARCHAR(255) UNIQUE)"
            );

            statement.executeBatch();
        }
    }
}
