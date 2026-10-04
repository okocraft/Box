package net.okocraft.box.storage.implementation.database.database.sqlite;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteLeaseTest {

    @Test
    void releasedLeaseCannotOperateOnTheNextLeasesConnection() throws Exception {
        MemorySQLiteDatabase database = MemorySQLiteDatabase.prepareDatabase();
        try {
            NonCloseableConnection released = (NonCloseableConnection) database.getConnection();
            assertFalse(released.isClosed());
            released.close();
            try (Connection current = database.getConnection()) {
                assertTrue(released.isClosed());
                assertFalse(released.isValid(1));
                released.close(); // Closing twice must not release the current lease.
                assertAll(
                    () -> assertThrows(SQLException.class, released::createStatement),
                    () -> assertThrows(SQLException.class, () -> released.prepareStatement("SELECT 1")),
                    () -> assertThrows(SQLException.class, released::getAutoCommit),
                    () -> assertThrows(SQLException.class, () -> released.setAutoCommit(false)),
                    () -> assertThrows(SQLException.class, released::commit),
                    () -> assertThrows(SQLException.class, released::rollback),
                    () -> assertThrows(SQLException.class, released::getMetaData),
                    () -> assertThrows(SQLException.class, () -> released.setTypeMap(Map.of())),
                    () -> assertThrows(SQLException.class, () -> released.setClientInfo(new Properties())),
                    () -> assertThrows(SQLException.class, () -> released.setClientInfo("name", "value")),
                    () -> assertThrows(SQLException.class, released::beginRequest),
                    () -> assertThrows(SQLException.class, released::endRequest),
                    () -> assertThrows(SQLException.class, () -> released.unwrap(Connection.class)),
                    () -> assertThrows(SQLException.class, () -> released.isWrapperFor(Connection.class)),
                    () -> assertThrows(SQLException.class, released::shutdown),
                    () -> assertThrows(SQLException.class, () -> released.abort(Runnable::run))
                );
                assertTrue(current.getAutoCommit());
                try (Statement statement = current.createStatement(); var rows = statement.executeQuery("SELECT 42")) {
                    assertTrue(rows.next());
                    assertEquals(42, rows.getInt(1));
                }
            }
        } finally {
            database.shutdown();
        }
    }

    @Test
    void jdbcObjectsAndUnwrapNeverExposeTheRawConnection() throws Exception {
        MemorySQLiteDatabase database = MemorySQLiteDatabase.prepareDatabase();
        try (Connection lease = database.getConnection(); Statement statement = lease.createStatement();
             var rows = statement.executeQuery("SELECT 1")) {
            assertSame(lease, lease.unwrap(Connection.class));
            assertTrue(lease.isWrapperFor(Connection.class));
            Class<?> driverConnection = Class.forName("org.sqlite.SQLiteConnection");
            assertFalse(lease.isWrapperFor(driverConnection));
            assertThrows(SQLException.class, () -> lease.unwrap(driverConnection));
            assertSame(lease, statement.getConnection());
            assertSame(lease, lease.getMetaData().getConnection());
            assertSame(statement, rows.getStatement());
            assertSame(lease, rows.getStatement().getConnection());
            assertSame(statement, statement.unwrap(Statement.class));
            Class<?> driverStatement = Class.forName("org.sqlite.jdbc4.JDBC4Statement");
            assertFalse(statement.isWrapperFor(driverStatement));
            assertThrows(SQLException.class, () -> statement.unwrap(driverStatement));
            assertThrows(SQLException.class, () -> ((NonCloseableConnection) lease).shutdown());
            assertThrows(SQLException.class, () -> lease.abort(Runnable::run));
            assertFalse(lease.isClosed());
        } finally {
            database.shutdown();
        }
    }

    @Test
    void retainedStatementsResultsAndMetadataFailAfterRelease() throws Exception {
        MemorySQLiteDatabase database = MemorySQLiteDatabase.prepareDatabase();
        try {
            Connection released = database.getConnection();
            Statement statement = released.createStatement();
            var prepared = released.prepareStatement("SELECT ?");
            var metadata = released.getMetaData();
            var rows = statement.executeQuery("SELECT 1");
            released.close();
            try (Connection current = database.getConnection()) {
                assertTrue(statement.isClosed());
                assertTrue(rows.isClosed());
                assertDoesNotThrow(statement::close);
                assertDoesNotThrow(prepared::close);
                assertDoesNotThrow(rows::close);
                assertAll(
                    () -> assertThrows(SQLException.class, statement::getConnection),
                    () -> assertThrows(SQLException.class, () -> statement.execute("SELECT 1")),
                    () -> assertThrows(SQLException.class, () -> statement.unwrap(Statement.class)),
                    () -> assertThrows(SQLException.class, () -> prepared.setInt(1, 1)),
                    () -> assertThrows(SQLException.class, rows::next),
                    () -> assertThrows(SQLException.class, rows::getStatement),
                    () -> assertThrows(SQLException.class, metadata::getConnection),
                    () -> assertThrows(SQLException.class, metadata::getDatabaseProductName)
                );
                assertFalse(current.isClosed());
            }
        } finally {
            database.shutdown();
        }
    }
}
