package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.implementation.database.operator.ItemTableOperator;
import net.okocraft.box.storage.implementation.database.operator.MetaTableOperator;
import net.okocraft.box.storage.implementation.database.operator.OperatorProvider;
import net.okocraft.box.storage.implementation.database.operator.RemappedItemTableOperator;
import net.okocraft.box.storage.implementation.database.operator.StockHolderTableOperator;
import net.okocraft.box.storage.implementation.database.operator.StockTableOperator;
import net.okocraft.box.storage.implementation.database.operator.UserTableOperator;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class SQLiteOperators {

    static @NotNull OperatorProvider create(@NotNull String prefix) {
        return new OperatorProvider(
            new MetaTableOperator(prefix) {
                @Override
                protected @NotNull String upsertStatement(@NotNull String tableName) {
                    return "INSERT INTO `%s` (`key`, `value`) VALUES (?, ?) ON CONFLICT (`key`) DO UPDATE SET `value` = excluded.value".formatted(tableName);
                }
            },
            new UserTableOperator(prefix, "BLOB") {
                @Override
                protected @NotNull String upsertStatement(@NotNull String tableName) {
                    return "INSERT INTO `%s` (`uuid`, `username`) VALUES (?, ?) ON CONFLICT (`uuid`) DO UPDATE SET `username` = excluded.username".formatted(tableName);
                }
            },
            new ItemTableOperator(prefix),
            new CustomItemTableOperator(prefix),
            new RemappedItemTableOperator(prefix),
            new StockHolderTableOperator(prefix, "BLOB"),
            new StockTableOperator(prefix) {
                @Override
                protected @NotNull String upsertStockStatement(@NotNull String tableName) {
                    return "INSERT INTO `%s` (`stock_id`, `item_id`, `amount`) VALUES (?, ?, ?) ON CONFLICT (`stock_id`, `item_id`) DO UPDATE SET `amount` = excluded.amount".formatted(tableName);
                }
            },
            new CustomDataTableOperator(prefix, "_v2"),
            new CustomDataTableOperator(prefix, ""),
            new PatchingOperator(prefix)
        );
    }

    private interface ByteRW {
        default byte[] readBytes0(@NotNull ResultSet resultSet, int pos) throws SQLException {
            return resultSet.getBytes(pos);
        }

        default void writeBytes0(@NotNull PreparedStatement statement, int pos, byte[] data) throws SQLException {
            statement.setBytes(pos, data);
        }
    }

    private static final class CustomItemTableOperator extends net.okocraft.box.storage.implementation.database.operator.CustomItemTableOperator implements ByteRW {

        private CustomItemTableOperator(@NotNull String tablePrefix) {
            super(tablePrefix);
        }

        @Override
        protected byte[] readBytes(@NotNull ResultSet resultSet, int pos) throws SQLException {
            return this.readBytes0(resultSet, pos);
        }

        @Override
        protected void writeBytes(@NotNull PreparedStatement statement, int pos, byte[] data) throws SQLException {
            this.writeBytes0(statement, pos, data);
        }
    }

    private static final class CustomDataTableOperator extends net.okocraft.box.storage.implementation.database.operator.CustomDataTableOperator implements ByteRW {

        private CustomDataTableOperator(@NotNull String prefix, @NotNull String suffix) {
            super(prefix, suffix);
        }

        @Override
        protected @NotNull String upsertStatement(@NotNull String tableName) {
            return "INSERT INTO `%s` (`key`, `data`) VALUES (?, ?) ON CONFLICT (`key`) DO UPDATE SET `data` = excluded.data".formatted(tableName);
        }

        @Override
        protected byte[] readBytes(@NotNull ResultSet resultSet, int pos) throws SQLException {
            return this.readBytes0(resultSet, pos);
        }

        @Override
        protected void writeBytes(@NotNull PreparedStatement statement, int pos, byte[] data) throws SQLException {
            this.writeBytes0(statement, pos, data);
        }
    }

    private static final class PatchingOperator extends net.okocraft.box.storage.implementation.database.operator.PatchingOperator implements ByteRW {

        private PatchingOperator(@NotNull String prefix) {
            super(prefix);
        }

        @Override
        protected byte[] readBytes(@NotNull ResultSet resultSet, int pos) throws SQLException {
            return this.readBytes0(resultSet, pos);
        }
    }
}
