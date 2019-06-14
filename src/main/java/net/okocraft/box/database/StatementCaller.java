/*
 * Box
 * Copyright (C) 2019 AKANE AKAGI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.okocraft.box.database;

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
