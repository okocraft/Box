package net.okocraft.box.feature.stats.database.operator;

import net.okocraft.box.storage.implementation.database.database.Database;

import java.sql.Connection;
import java.sql.SQLException;

public record StatisticsOperators(
    StockStatisticsTableOperator stockStatisticsTableOperator,
    ItemStatisticsOperator itemStatisticsTableOperator
) {

    public static StatisticsOperators create(Database database) {
        return new StatisticsOperators(
            new StockStatisticsTableOperator(database.tablePrefix(), database.operators().stockTable().tableName(), database.operators().stockHolderTable().tableName()),
            new ItemStatisticsOperator(database.operators().stockTable().tableName())
        );
    }

    public void initTables(Database database) throws SQLException {
        try (Connection connection = database.getConnection()) {
            this.stockStatisticsTableOperator.initTable(connection);
            this.stockStatisticsTableOperator.deleteNonExistingStockRecords(connection);
            this.stockStatisticsTableOperator.updateTableRecords(connection);
        }
    }
}
