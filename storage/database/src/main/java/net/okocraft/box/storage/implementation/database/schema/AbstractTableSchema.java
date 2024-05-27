package net.okocraft.box.storage.implementation.database.schema;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class AbstractTableSchema implements TableSchema {

    private final String tableName;

    protected AbstractTableSchema(@NotNull String tableName) {
        this.tableName = tableName;
    }

    @Override
    public @NotNull String tableName() {
        return this.tableName;
    }

    @Override
    public @NotNull List<String> createIndexStatements() {
        return Collections.emptyList();
    }
}
