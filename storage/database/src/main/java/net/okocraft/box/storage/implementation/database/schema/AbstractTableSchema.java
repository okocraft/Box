package net.okocraft.box.storage.implementation.database.schema;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractTableSchema implements TableSchema {

    private final String tableName;

    protected AbstractTableSchema(@NotNull String tableName) {
        this.tableName = tableName;
    }

    @Override
    public @NotNull String tableName() {
        return tableName;
    }

    @Override
    public @NotNull String createIndexStatement() {
        return "";
    }
}
