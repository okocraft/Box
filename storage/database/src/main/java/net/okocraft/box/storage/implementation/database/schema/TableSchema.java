package net.okocraft.box.storage.implementation.database.schema;

import org.jetbrains.annotations.NotNull;

public interface TableSchema {

    @NotNull String tableName();

    @NotNull String createTableStatement();

    @NotNull String createIndexStatement();

}
