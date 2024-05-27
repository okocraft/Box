package net.okocraft.box.storage.implementation.database.database;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.implementation.database.schema.SchemaSet;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface Database {

    void prepare() throws Exception;

    void shutdown() throws Exception;

    @NotNull Type getType();

    @NotNull List<Storage.Property> getInfo();

    @NotNull SchemaSet getSchemaSet();

    @NotNull Connection getConnection() throws SQLException;

    enum Type {
        MYSQL("mysql"),
        SQLITE("sqlite");

        private final String name;

        Type(@NotNull String name) {
            this.name = name;
        }

        public @NotNull String getName() {
            return this.name;
        }
    }
}
