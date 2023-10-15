package net.okocraft.box.storage.implementation.database.schema;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TableSchema {

    @NotNull String tableName();

    @NotNull String createTableStatement();

    @NotNull List<String> createIndexStatements();

}
