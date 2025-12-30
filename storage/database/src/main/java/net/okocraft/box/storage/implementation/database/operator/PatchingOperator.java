package net.okocraft.box.storage.implementation.database.operator;

import net.okocraft.box.storage.api.model.item.ItemData;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("SqlSourceToSinkFlow")
public abstract class PatchingOperator {

    private final String prefix;

    public PatchingOperator(@NotNull String prefix) {
        this.prefix = prefix;
    }

    public boolean hasTable(@NotNull Connection connection, @NotNull String tableType) throws SQLException {
        try (ResultSet result = connection.getMetaData().getTables(null, null, this.prefix + tableType, null)) {
            return result.next();
        }
    }

    public void renameTable(@NotNull Connection connection, @NotNull String oldTableType, @NotNull String newTableType) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE `%s` RENAME TO `%s`".formatted(this.prefix + oldTableType, this.prefix + newTableType));
        }
    }

    public void getDefaultItemsFromLegacy(@NotNull Connection connection, @NotNull String tableType, @NotNull BiConsumer<Integer, String> consumer) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT id, name FROM `%s` WHERE is_default_item=TRUE".formatted(this.prefix + tableType))) {
                while (resultSet.next()) {
                    consumer.accept(resultSet.getInt(1), resultSet.getString(2));
                }
            }
        }
    }

    public void getCustomItemsFromLegacyItemTable(@NotNull Connection connection, @NotNull String tableType, @NotNull Consumer<ItemData> consumer) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT id, name, item_data FROM `%s` WHERE is_default_item=FALSE".formatted(this.prefix + tableType))) {
                while (resultSet.next()) {
                    consumer.accept(new ItemData(resultSet.getInt(1), resultSet.getString(2), this.readBytes(resultSet, 3)));
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected abstract byte[] readBytes(@NotNull ResultSet resultSet, int pos) throws SQLException;
}
