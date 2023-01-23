package net.okocraft.box.storage.implementation.database.database.mysql;

import net.okocraft.box.storage.implementation.database.schema.AbstractTableSchema;
import net.okocraft.box.storage.implementation.database.schema.SchemaSet;
import org.jetbrains.annotations.NotNull;

public final class MySQLTableSchema {

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
            super(tablePrefix + "meta");
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
            super(tablePrefix + "items");
        }

        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE `%table%` (
                      `id` INT PRIMARY KEY AUTO_INCREMENT,
                      `name` VARCHAR(50) NOT NULL,
                      `item_data` BLOB NOT NULL,
                      `is_default_item` INT NOT NULL
                    )
                    """;
        }
    }

    private static class Stock extends AbstractTableSchema {

        private Stock(@NotNull String tablePrefix) {
            super(tablePrefix + "stock");
        }


        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE `%table%` (
                      `uuid` VARCHAR(36) NOT NULL,
                      `item_id` INT  NOT NULL,
                      `amount` INT NOT NULL,
                      PRIMARY KEY (`uuid`, `item_id`)
                    )
                    """;
        }
    }

    private static class CustomData extends AbstractTableSchema {

        private CustomData(@NotNull String tablePrefix) {
            super(tablePrefix + "custom_data");
        }


        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE `%table%` (
                      `key` VARCHAR(50) PRIMARY KEY NOT NULL,
                      `data` BLOB  NOT NULL
                    )
                    """;
        }
    }
}
