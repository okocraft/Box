package net.okocraft.box.database;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @since 1.0.0-SNAPSHOT
 * @author akaregi
 */
public class StatementRunner implements Runnable {
    private final Statement statement;

    public StatementRunner(Statement statement) {
        this.statement = statement;
    }

    @Override
    public void run() {
        try (Statement stmt = statement) {
            statement.executeBatch();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

}
