package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.storage.implementation.database.schema.AbstractTableSchema;
import net.okocraft.box.storage.implementation.database.schema.SchemaSet;
import org.jetbrains.annotations.NotNull;

public final class SQLiteTableSchema {

    public static @NotNull SchemaSet create(@NotNull String tablePrefix) {
        return new SchemaSet(
                new Meta(tablePrefix), new Users(tablePrefix), new Items(tablePrefix),
                new Stock(tablePrefix), new CustomData(tablePrefix)
        );
    }

    private static class Users extends AbstractTableSchema {

        public Users(@NotNull String tablePrefix) {
            super(tablePrefix + "users");
        }

        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE `%table%` (
                      `uuid` VARCHAR(36) PRIMARY KEY NOT NULL,
                      `username` VARCHAR(16) NOT NULL
                    )
                    """;
        }

        @Override
        public @NotNull String createIndexStatement() {
            return "CREATE INDEX `%table%_username` ON `%table%` (`username`)";
        }
    }

    private static class Meta extends AbstractTableSchema {

        private Meta(@NotNull String tablePrefix) {
            super(tablePrefix + "_meta");
        }

        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE `%table%` (
                      `key` VARCHAR(25) PRIMARY KEY NOT NULL,
                      `value` VARCHAR(16) NOT NULL
                    )
                    """;
        }
    }

    private static class Items extends AbstractTableSchema {

        private Items(@NotNull String tablePrefix) {
            super(tablePrefix + "_items");
        }

        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE `%table%` (
                      `id` INTEGER PRIMARY KEY NOT NULL AUTOINCREMENT,
                      `name` VARCHAR(50) NOT NULL,
                      `item_data` BLOB NOT NULL,
                      `is_default_item` INTEGER NOT NULL
                    )
                    """;
        }
    }

    private static class Stock extends AbstractTableSchema {

        private Stock(@NotNull String tablePrefix) {
            super(tablePrefix + "_stock");
        }


        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE `%table%` (
                      `uuid` VARCHAR(36) NOT NULL,
                      `item_id` INTEGER  NOT NULL,
                      `amount` INTEGER NOT NULL,
                      PRIMARY KEY (`uuid`, `item_id`)
                    )
                    """;
        }
    }

    private static class CustomData extends AbstractTableSchema {

        private CustomData(@NotNull String tablePrefix) {
            super(tablePrefix + "_custom_data");
        }


        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE `%table%` (
                      `key` VARCHAR(50) PRIMARY KEY NOT NULL,
                      `value` BLOB  NOT NULL
                    )
                    """;
        }
    }
}
