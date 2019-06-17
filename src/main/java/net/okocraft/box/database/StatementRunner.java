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

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @since 1.0.0-SNAPSHOT
 * @author akaregi
 */
public class StatementRunner implements Runnable {
    private final Statement statement;

    StatementRunner(Statement statement) {
        this.statement = statement;
    }

    @Override
    public void run() {
        try (statement) {
            statement.executeBatch();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
