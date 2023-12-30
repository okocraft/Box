package net.okocraft.box.storage.implementation.database.table;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxDefaultItem;
import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.factory.item.BoxItemFactory;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// | id | name | item_data | is_default_item
public class ItemTable extends AbstractTable implements ItemStorage {

    private final MetaTable metaTable;

    public ItemTable(@NotNull Database database, @NotNull MetaTable metaTable) {
        super(database, database.getSchemaSet().itemTable());
        this.metaTable = metaTable;
    }

    @Override
    public void init() throws Exception {
        this.createTableAndIndex();
    }

    @Override
    public @NotNull Optional<ItemVersion> getItemVersion() throws Exception {
        var dataVersion = this.metaTable.getItemDataVersion();
        int defaultItemProviderVersion = this.metaTable.getDefaultItemProviderVersion();
        return dataVersion != null ? Optional.of(new ItemVersion(dataVersion, defaultItemProviderVersion)) : Optional.empty();
    }

    @Override
    public void saveItemVersion(@NotNull ItemVersion itemVersion) throws Exception {
        this.metaTable.saveItemDataVersion(itemVersion.dataVersion().dataVersion());
        this.metaTable.saveDefaultItemVersion(itemVersion.defaultItemVersion());
    }

    @Override
    public <I> @NotNull List<I> loadAllDefaultItems(@NotNull Function<ItemData, I> function) throws Exception {
        var result = new ArrayList<I>(1000); // The number of default items is currently around 1400, so this should only need to be expanded once.

        try (var connection = this.database.getConnection();
             var statement = prepareStatement(connection, "SELECT `id`, `name`, `item_data` FROM `%table%` WHERE `is_default_item`=TRUE")) {
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(function.apply(this.toItemData(rs)));
                }
            }
        }

        return result;
    }

    @Override
    public @NotNull List<BoxDefaultItem> saveDefaultItems(@NotNull List<DefaultItem> newItems, @NotNull Int2ObjectMap<DefaultItem> updatedItemMap) throws Exception {
        var result = new ArrayList<BoxDefaultItem>(newItems.size() + updatedItemMap.size());

        try (var connection = this.database.getConnection()) {
            try (var statement = prepareStatement(connection, "UPDATE `%table%` SET `name`=?, `item_data`=? WHERE `id`=?")) {
                for (var entry : updatedItemMap.int2ObjectEntrySet()) {
                    int internalId = entry.getIntKey();
                    var item = entry.getValue();

                    statement.setString(1, item.plainName());
                    writeBytesToStatement(statement, 2, item.itemStack().serializeAsBytes());
                    statement.setInt(3, internalId);

                    statement.addBatch();
                    result.add(BoxItemFactory.createDefaultItem(internalId, item));
                }

                statement.executeBatch();
            }

            try (var statement = prepareStatement(connection, "INSERT INTO `%table%` (`name`, `item_data`, `is_default_item`) VALUES(?,?,?)")) {
                for (var item : newItems) {
                    statement.setString(1, item.plainName());
                    writeBytesToStatement(statement, 2, item.itemStack().serializeAsBytes());
                    statement.setBoolean(3, true);

                    statement.addBatch();
                }

                int[] updateCounts = statement.executeBatch();

                try (var generatedKeys = statement.getGeneratedKeys()) {
                    for (int i = 0; i < updateCounts.length; i++) {
                        if (updateCounts[i] == Statement.RETURN_GENERATED_KEYS) {
                            result.add(BoxItemFactory.createDefaultItem(generatedKeys.getInt(1), newItems.get(i)));
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public <I> @NotNull List<I> loadAllCustomItems(@NotNull Function<ItemData, I> function) throws Exception {
        var result = new ArrayList<I>();

        try (var connection = this.database.getConnection();
             var statement = prepareStatement(connection, "SELECT `id`, `name`, `item_data` FROM `%table%` WHERE `is_default_item`=FALSE")) {
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(function.apply(this.toItemData(rs)));
                }
            }
        }

        return result;
    }

    @Override
    public void updateCustomItems(@NotNull Collection<BoxCustomItem> items) throws Exception {
        try (var connection = this.database.getConnection();
             var statement = prepareStatement(connection, "UPDATE `%table%` SET `name`=?, `item_data`=? WHERE `id`=?")) {

            for (var item : items) {
                statement.setString(1, item.getPlainName());
                writeBytesToStatement(statement, 2, item.getOriginal().serializeAsBytes());
                statement.setInt(3, item.getInternalId());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    @Override
    public @NotNull BoxCustomItem saveNewCustomItem(@NotNull ItemStack item, @Nullable String itemName) throws Exception {
        try (var connection = this.database.getConnection()) {
            var itemBytes = item.serializeAsBytes();
            var name = itemName != null ? itemName : ItemNameGenerator.itemStack(item.getType(), itemBytes);

            try (var statement = prepareStatement(connection, "INSERT INTO `%table%` (`name`, `item_data`, `is_default_item`) VALUES(?,?,?)")) {
                statement.setString(1, name);
                writeBytesToStatement(statement, 2, itemBytes);
                statement.setBoolean(3, false);

                statement.executeUpdate();

                try (var generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return BoxItemFactory.createCustomItem(generatedKeys.getInt(1), name, item);
                    } else {
                        throw new Exception("Could not get an item id.");
                    }
                }
            }
        }
    }

    @Override
    public @NotNull BoxCustomItem renameCustomItem(@NotNull BoxCustomItem item, @NotNull String newName) throws Exception {
        try (var connection = this.database.getConnection();
             var statement = prepareStatement(connection, "UPDATE `%table%` SET `name`=? WHERE `id`=?")) {

            statement.setString(1, newName);
            statement.setInt(2, item.getInternalId());

            statement.execute();
        }

        BoxItemFactory.renameCustomItem(item, newName);
        return item;
    }

    private @NotNull ItemData toItemData(@NotNull ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        var name = resultSet.getString("name");
        var itemData = readBytesFromResultSet(resultSet, "item_data");

        return new ItemData(id, name, itemData);
    }
}
