package net.okocraft.box.feature.stats.database.operator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TargetedStockStatisticsTest {
    @Test
    void targetKeepsGlobalRanksAndTotals() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE stock (stock_id INT, item_id INT, amount INT)");
            statement.execute("INSERT INTO stock VALUES (1,1,10),(2,1,90),(1,2,30),(3,2,70),(4,3,100)");
            var operator = new StockStatisticsTableOperator("", "stock", "holders");
            operator.initTable(connection);
            operator.updateTableRecordsByStockIds(connection, IntArrayList.of(1));
            try (var rows = statement.executeQuery("SELECT item_id, rank, percentage FROM stock_statistics ORDER BY item_id")) {
                assertEquals(true, rows.next());
                assertEquals(1, rows.getInt(1));
                assertEquals(2, rows.getInt(2));
                assertEquals(10.0, rows.getDouble(3), 0.001);
                assertEquals(true, rows.next());
                assertEquals(2, rows.getInt(1));
                assertEquals(2, rows.getInt(2));
                assertEquals(30.0, rows.getDouble(3), 0.001);
                assertEquals(false, rows.next());
            }
        }
    }
}
