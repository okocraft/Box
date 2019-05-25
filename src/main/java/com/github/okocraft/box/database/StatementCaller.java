package com.github.okocraft.box.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.Callable;

public class StatementCaller implements Callable<Optional<ResultSet>> {
    private final PreparedStatement statement;

    public StatementCaller(PreparedStatement statement) {
        this.statement = statement;
    }

    @Override
    public Optional<ResultSet> call() {
        try {
            return Optional.of(statement.executeQuery());
        } catch (SQLException exception) {
            // Do nothing.
        }

        return Optional.empty();
    }
}
