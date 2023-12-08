package net.okocraft.box.storage.implementation.database.schema;

import org.jetbrains.annotations.NotNull;

public record SchemaSet(@NotNull TableSchema metaTable, @NotNull TableSchema userTable,
                        @NotNull TableSchema itemTable, @NotNull TableSchema stockTable,
                        @NotNull TableSchema customDataTable,
                        @NotNull TableSchema legacyCustomDataTable) {
}
