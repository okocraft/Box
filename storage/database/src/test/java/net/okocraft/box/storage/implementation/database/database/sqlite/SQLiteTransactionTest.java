package net.okocraft.box.storage.implementation.database.database.sqlite;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.implementation.database.table.StockTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.sql.Connection;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteTransactionTest {

    @Test
    void fileDatabasePreparationReleasesItsConnectionLease(@TempDir Path directory) throws Exception {
        var storage = SQLiteDatabase.createStorage(new StorageContext<>(directory, new SQLiteSetting("box_", "test.db")));
        storage.init();
        try (var executor = Executors.newSingleThreadExecutor()) {
            assertTrue(executor.submit(() -> {
                try (Connection connection = storage.getDatabase().getConnection()) {
                    return connection.getAutoCommit();
                }
            }).get(5, TimeUnit.SECONDS));
        } finally {
            storage.close();
        }
    }

    @Test
    void savesLargePartialBatchesWithOneCommitAndPreservesOtherItems() throws Exception {
        Connection connection = Mockito.spy(AbstractSQLiteDatabase.newConnection("memory", ":memory:"));
        AbstractSQLiteDatabase database = new AbstractSQLiteDatabase("box_") {
            @Override public void prepare() throws Exception { this.connect(); }
            @Override public void shutdown() throws Exception { this.disconnect(); }
            @Override protected Connection createConnection() { return connection; }
            @Override public List<Storage.Property> getInfo() { return List.of(); }
        };
        database.prepare();
        try {
            StockTable table = new StockTable(database);
            try (Connection lease = database.getConnection()) { table.init(lease); }
            UUID owner = UUID.randomUUID();
            table.saveStockData(owner, List.of(new StockData(2000, 64)));
            Mockito.clearInvocations(connection);

            List<StockData> batch = IntStream.rangeClosed(1, 1000)
                .mapToObj(id -> new StockData(id, id)).toList();
            table.savePartialStockData(owner, batch);
            Mockito.verify(connection, Mockito.times(1)).commit();
            assertTrue(connection.getAutoCommit());
            HashSet<StockData> expected = new HashSet<>(batch);
            expected.add(new StockData(2000, 64));
            assertEquals(expected, new HashSet<>(table.loadStockData(owner)));

            table.saveStockData(owner, List.of(new StockData(1, 42)));
            assertEquals(List.of(new StockData(1, 42)), table.loadStockData(owner));
            assertTrue(connection.getAutoCommit());
        } finally {
            database.shutdown();
        }
    }

    @Test
    void concurrentReadersWaitUntilTheTransactionLeaseCloses() throws Exception {
        MemorySQLiteDatabase database = MemorySQLiteDatabase.prepareDatabase();
        try (var executor = Executors.newSingleThreadExecutor()) {
            CountDownLatch started = new CountDownLatch(1);
            java.util.concurrent.Future<Boolean> reader;
            try (Connection lease = database.getConnection()) {
                lease.setAutoCommit(false);
                reader = executor.submit(() -> {
                    started.countDown();
                    try (Connection next = database.getConnection()) { return next.getAutoCommit(); }
                });
                assertTrue(started.await(5, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> reader.get(100, TimeUnit.MILLISECONDS));
                lease.rollback();
                lease.setAutoCommit(true);
            }
            assertTrue(reader.get(5, TimeUnit.SECONDS));
        } finally {
            database.shutdown();
        }
    }
}
