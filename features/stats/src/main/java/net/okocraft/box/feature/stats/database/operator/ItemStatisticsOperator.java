package net.okocraft.box.feature.stats.database.operator;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;

public class ItemStatisticsOperator {

    private final String selectTotalAmountByItemIdQuery;

    public ItemStatisticsOperator(String stockTableName) {
        this.selectTotalAmountByItemIdQuery = """
            SELECT
                item_id,
                SUM(amount) as total_amount
            FROM %1$s
            GROUP BY item_id
            """.formatted(stockTableName);
    }

    public void selectTotalAmountByItemId(@NotNull Connection connection, @NotNull BiConsumer<Integer, Long> consumer) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(this.selectTotalAmountByItemIdQuery)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int itemId = resultSet.getInt("item_id");
                    long totalAmount = resultSet.getLong("total_amount");
                    consumer.accept(itemId, totalAmount);
                }
            }
        }
    }
}
