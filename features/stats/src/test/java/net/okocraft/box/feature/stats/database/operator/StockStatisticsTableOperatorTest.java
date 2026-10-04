package net.okocraft.box.feature.stats.database.operator;

import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StockStatisticsTableOperatorTest {
    @Test
    void percentagesUseTheWholeItemPartition() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE stock (stock_id INT, item_id INT, amount INT)");
            statement.execute("INSERT INTO stock VALUES (1,1,90),(2,1,10),(1,2,50),(2,2,50)");
            var operator = new StockStatisticsTableOperator("", "stock", "holders");
            operator.initTable(connection);
            operator.updateTableRecords(connection);
            try (var rows = statement.executeQuery("SELECT percentage, rank FROM stock_statistics ORDER BY item_id, stock_id")) {
                double[] percentages = {90, 10, 50, 50};
                int[] ranks = {1, 2, 1, 1};
                int count = 0;
                while (rows.next()) {
                    assertEquals(percentages[count], rows.getDouble(1), 0.001);
                    assertEquals(ranks[count], rows.getInt(2));
                    count++;
                }
                assertEquals(4, count);
            }
        }
    }
}
