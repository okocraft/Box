package net.okocraft.box.storage.implementation.database.database.mysql;

import net.okocraft.box.storage.implementation.database.schema.AbstractTableSchema;
import net.okocraft.box.storage.implementation.database.schema.SchemaSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MySQLTableSchema {

    public static @NotNull SchemaSet create(@NotNull String tablePrefix) {
        return new SchemaSet(
                new Meta(tablePrefix), new Users(tablePrefix), new Items(tablePrefix),
                new Stock(tablePrefix), new CustomData(tablePrefix), new LegacyCustomData(tablePrefix)
        );
    }

    private static class Users extends AbstractTableSchema {

        public Users(@NotNull String tablePrefix) {
            super(tablePrefix + "users");
        }

        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE  IF NOT EXISTS `%table%` (
                      `uuid` VARCHAR(36) PRIMARY KEY NOT NULL,
                      `username` VARCHAR(16) NOT NULL
                    )
                    """;
        }

        @Override
        public @NotNull List<String> createIndexStatements() {
            return List.of("CREATE INDEX IF NOT EXISTS `%table%_username` ON `%table%` (`username`)");
        }
    }

    private static class Meta extends AbstractTableSchema {

        private Meta(@NotNull String tablePrefix) {
            super(tablePrefix + "meta");
        }

        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE  IF NOT EXISTS `%table%` (
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
                    CREATE TABLE  IF NOT EXISTS `%table%` (
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
                    CREATE TABLE  IF NOT EXISTS `%table%` (
                      `uuid` VARCHAR(36) NOT NULL,
                      `item_id` INT  NOT NULL,
                      `amount` INT NOT NULL,
                      PRIMARY KEY (`uuid`, `item_id`)
                    )
                    """;
        }

        @Override
        public @NotNull List<String> createIndexStatements() {
            return List.of("CREATE INDEX IF NOT EXISTS `%table%_amount` ON `%table%` (`amount`)");
        }
    }

    private static class CustomData extends AbstractTableSchema {

        private CustomData(@NotNull String tablePrefix) {
            super(tablePrefix + "custom_data_v2");
        }

        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE IF NOT EXISTS `%table%` (
                      `key` VARCHAR(50) PRIMARY KEY NOT NULL,
                      `data` BLOB  NOT NULL
                    )
                    """;
        }
    }

    private static class LegacyCustomData extends AbstractTableSchema {

        private LegacyCustomData(@NotNull String tablePrefix) {
            super(tablePrefix + "custom_data");
        }

        @Override
        public @NotNull String createTableStatement() {
            return """
                    CREATE TABLE IF NOT EXISTS `%table%` (
                      `key` VARCHAR(50) PRIMARY KEY NOT NULL,
                      `data` BLOB  NOT NULL
                    )
                    """;
        }
    }
}
