package net.okocraft.box.feature.stats.database.operator;

import it.unimi.dsi.fastutil.ints.IntCollection;
import net.okocraft.box.feature.stats.model.StockStatistics;
import net.okocraft.box.storage.implementation.database.operator.UUIDConverters;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.BiConsumer;

public class StockStatisticsTableOperator {

    private static final String BULK_UPSERT_QUERY = """
        INSERT INTO %1$s (stock_id, item_id, amount, rank, percentage)
            WITH global_stock_data AS (
                SELECT
                    stock_id,
                    item_id,
                    amount,
                    RANK() OVER stock_amount_by_item_id as rank,
                    SUM(amount) OVER stock_amount_by_item_id as total_amount
                FROM %2$s
                WINDOW stock_amount_by_item_id AS (PARTITION BY item_id ORDER BY amount DESC)
            )
            SELECT
                stock_id,
                item_id,
                amount,
                rank,
                ROUND(
                    CASE
                        WHEN total_amount = 0 THEN 0
                        ELSE amount * 100.0 / total_amount
                    END,
                    2
                ) as percentage
            FROM global_stock_data
            {WHERE_CLAUSE}
        ON CONFLICT(stock_id, item_id) DO UPDATE SET
            amount = EXCLUDED.amount,
            rank = EXCLUDED.rank,
            percentage = EXCLUDED.percentage
        """;

    private final String createTableQuery;
    private final String createItemIdIndexQuery;
    private final String bulkUpsertQuery;
    private final String bulkUpsertByStockIdQuery;
    private final String deleteNonExistingStockRecordsQuery;
    private final String selectRecordsByUuid;

    public StockStatisticsTableOperator(String tablePrefix, String stockTableName, String stockHolderTableName) {
        String tableName = tablePrefix + "stock_statistics";

        this.createTableQuery = """
            CREATE TABLE IF NOT EXISTS %1$s (
                stock_id INT NOT NULL,
                item_id INT NOT NULL,
                amount INT NOT NULL,
                rank INT NOT NULL,
                percentage FLOAT NOT NULL,
                PRIMARY KEY (`stock_id`, `item_id`)
            )
            """.formatted(tableName);
        this.createItemIdIndexQuery = "CREATE INDEX IF NOT EXISTS idx_%1$s_item_id_rank ON %1$s (item_id, rank)".formatted(tableName);
        this.bulkUpsertQuery = BULK_UPSERT_QUERY.formatted(tableName, stockTableName).replace("{WHERE_CLAUSE}", "WHERE TRUE");
        this.bulkUpsertByStockIdQuery = BULK_UPSERT_QUERY.formatted(tableName, stockTableName);
        this.deleteNonExistingStockRecordsQuery = """
            DELETE FROM %1$s
            WHERE NOT EXISTS (
                SELECT 1
                FROM %2$s
                WHERE %1$s.stock_id = %2$s.stock_id AND %1$s.item_id = %2$s.item_id
            )
            """.formatted(tableName, stockTableName);
        this.selectRecordsByUuid = """
            SELECT %1$s.item_id as item_id, %1$s.amount as amount, %1$s.rank as rank, %1$s.percentage as percentage
            FROM %1$s
            INNER JOIN %2$s ON %1$s.stock_id = %2$s.stock_id AND %1$s.item_id = %2$s.item_id
            INNER JOIN %3$s ON %2$s.stock_id = %3$s.id
            WHERE %3$s.uuid = ?
            """.formatted(tableName, stockTableName, stockHolderTableName);
    }

    public void initTable(@NotNull Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(this.createTableQuery);
            statement.execute(this.createItemIdIndexQuery);
        }
    }

    public void updateTableRecords(@NotNull Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(this.bulkUpsertQuery);
        }
    }

    public void updateTableRecordsByStockIds(@NotNull Connection connection, IntCollection stockIds) throws SQLException {
        StringBuilder whereClause = new StringBuilder("WHERE stock_id IN (");
        boolean first = true;
        for (int stockId : stockIds) {
            if (!first) {
                whereClause.append(", ");
            }
            whereClause.append(stockId);
            first = false;
        }
        whereClause.append(")");

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(this.bulkUpsertByStockIdQuery.replace("{WHERE_CLAUSE}", whereClause.toString()));
        }
    }

    public void deleteNonExistingStockRecords(@NotNull Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(this.deleteNonExistingStockRecordsQuery);
        }
    }

    public void selectRecordsByUuid(@NotNull Connection connection, @NotNull UUID uuid, @NotNull BiConsumer<Integer, StockStatistics> consumer) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(this.selectRecordsByUuid)) {
            statement.setBytes(1, UUIDConverters.toBytes(uuid));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int itemId = resultSet.getInt("item_id");
                    int amount = resultSet.getInt("amount");
                    int rank = resultSet.getInt("rank");
                    float percentage = resultSet.getFloat("percentage");
                    consumer.accept(itemId, new StockStatistics(amount, rank, percentage));
                }
            }
        }
    }
}
