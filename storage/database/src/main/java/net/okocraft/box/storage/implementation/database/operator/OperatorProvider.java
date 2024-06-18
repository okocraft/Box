package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNull;

public record OperatorProvider(
        @NotNull MetaTableOperator metaTable,
        @NotNull UserTableOperator userTable,
        @NotNull ItemTableOperator itemTable,
        @NotNull CustomItemTableOperator customItemTable,
        @NotNull RemappedItemTableOperator remappedItemTable,
        @NotNull StockTableOperator stockTable,
        @NotNull CustomDataTableOperator customDataTable,
        @NotNull CustomDataTableOperator legacyCustomDataTable,
        @NotNull PatchingOperator patcher
) {
}
