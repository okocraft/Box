package net.okocraft.box.storage.implementation.database.operator;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

public final class Transactions {

    public static void execute(Connection connection, Operation operation) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        Savepoint savepoint = null;
        if (autoCommit) {
            connection.setAutoCommit(false);
        } else {
            savepoint = connection.setSavepoint();
        }

        try {
            operation.run();
            if (autoCommit) {
                connection.commit();
            } else {
                connection.releaseSavepoint(savepoint);
            }
        } catch (SQLException | RuntimeException | Error failure) {
            try {
                if (autoCommit) connection.rollback();
                else connection.rollback(savepoint);
            } catch (SQLException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
            throw failure;
        } finally {
            if (autoCommit) connection.setAutoCommit(true);
        }
    }

    @FunctionalInterface
    public interface Operation {
        void run() throws SQLException;
    }

    private Transactions() {
    }
}
