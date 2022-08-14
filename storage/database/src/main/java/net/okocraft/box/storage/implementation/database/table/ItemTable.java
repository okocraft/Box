package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.ItemNameGenerator;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// | id | name | item_data | is_default_item
public class ItemTable extends AbstractTable implements ItemStorage {

    private final MetaTable metaTable;

    public ItemTable(@NotNull Database database, @NotNull MetaTable metaTable) {
        super(database, database.getSchemaSet().itemTable());
        this.metaTable = metaTable;
    }

    @Override
    public void init() throws Exception {
        createTableAndIndex();
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public int getDataVersion() throws Exception {
        return metaTable.getItemDataVersion();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void saveCurrentDataVersion() throws Exception {
        metaTable.saveItemDataVersion(Bukkit.getUnsafe().getDataVersion());
    }

    @Override
    public @NotNull List<BoxItem> loadAllDefaultItems() throws Exception {
        var result = new ArrayList<BoxItem>();

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT id, name, item_data FROM `%table%` WHERE is_default_item=TRUE")) {
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(readResultSet(rs, true));
                }
            }
        }

        return result;
    }

    @Override
    public @NotNull List<BoxItem> updateDefaultItems(@NotNull Map<BoxItem, DefaultItem> itemMap) throws Exception {
        var result = new ArrayList<BoxItem>();

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "UPDATE `%table%` SET name=?, item_data=? WHERE id=?")) {

            for (var entry : itemMap.entrySet()) {
                var boxItem = entry.getKey();
                var defaultItem = entry.getValue();

                var itemBlob = connection.createBlob();
                itemBlob.setBytes(1, defaultItem.itemStack().serializeAsBytes());

                statement.setString(1, defaultItem.plainName());
                statement.setBlob(2, itemBlob);
                statement.setInt(3, boxItem.getInternalId());

                statement.addBatch();
                result.add(BoxItemFactory.createDefaultItem(defaultItem, boxItem.getInternalId()));
            }

            statement.executeBatch();
        }

        return result;
    }

    @Override
    public @NotNull List<BoxItem> saveNewDefaultItems(@NotNull List<DefaultItem> newItems) throws Exception {
        var result = new ArrayList<BoxItem>();

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "INSERT INTO `%table%` (name, item_data, is_default_item) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {

            for (var defaultItem : newItems) {
                statement.setString(1, defaultItem.plainName());

                var itemBlob = connection.createBlob();
                itemBlob.setBytes(1, defaultItem.itemStack().serializeAsBytes());

                statement.setBlob(2, itemBlob);
                statement.setBoolean(3, true);

                statement.addBatch();
            }

            statement.executeBatch();

            try (var resultSet = statement.getGeneratedKeys()) {
                int index = 0;
                int itemListSize = newItems.size();

                while (resultSet.next() && index < itemListSize) {
                    int id = resultSet.getInt(1);
                    var item = newItems.get(index);
                    result.add(BoxItemFactory.createDefaultItem(item, id));
                    index++;
                }
            }
        }

        return result;
    }

    @Override
    public @NotNull List<BoxCustomItem> loadAllCustomItems() throws Exception {
        var result = new ArrayList<BoxCustomItem>();

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT id, name, item_data FROM `%table%` WHERE is_default_item=FALSE")) {
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add((BoxCustomItem) readResultSet(rs, false));
                }
            }
        }

        return result;
    }

    @Override
    public void updateCustomItems(@NotNull Collection<BoxCustomItem> items) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "UPDATE `%table%` SET name=?, item_data=? WHERE id=?")) {

            for (var item : items) {
                var itemBlob = connection.createBlob();
                itemBlob.setBytes(1, item.getOriginal().serializeAsBytes());

                statement.setString(1, item.getPlainName());
                statement.setBlob(2, itemBlob);
                statement.setInt(3, item.getInternalId());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    @Override
    public @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "INSERT INTO `%table%` (name, item_data, is_default_item) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            var itemBytes = item.serializeAsBytes();

            var itemName = ItemNameGenerator.generate(item.getType().name(), itemBytes);
            var itemBlob = connection.createBlob();
            itemBlob.setBytes(1, itemBytes);

            statement.setString(1, itemName);
            statement.setBlob(2, itemBlob);
            statement.setBoolean(3, false);

            statement.execute();
            var resultSet = statement.getGeneratedKeys();

            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                return BoxItemFactory.createCustomItem(item, itemName, id);
            }
        }

        throw new IllegalStateException("Could not create BoxCustomItem");
    }

    @Override
    public @NotNull BoxCustomItem rename(@NotNull BoxCustomItem item, @NotNull String newName) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "UPDATE `%table%` SET name=? WHERE id=?")) {

            statement.setString(1, newName);
            statement.setInt(2, item.getInternalId());

            statement.execute();
        }

        BoxItemFactory.renameCustomItem(item, newName);
        return item;
    }

    private @NotNull BoxItem readResultSet(@NotNull ResultSet resultSet, boolean isDefaultItem) throws SQLException {
        int id = resultSet.getInt("id");
        var name = resultSet.getString("name");
        var itemDataBlob = resultSet.getBlob("item_data");

        if (Integer.MAX_VALUE < itemDataBlob.length()) {
            throw new IllegalStateException("item data is too long: " + itemDataBlob.length());
        }

        var item = ItemStack.deserializeBytes(itemDataBlob.getBytes(1, (int) itemDataBlob.length()));

        return isDefaultItem ?
                BoxItemFactory.createDefaultItem(item, name, id) :
                BoxItemFactory.createCustomItem(item, name, id);
    }
}
