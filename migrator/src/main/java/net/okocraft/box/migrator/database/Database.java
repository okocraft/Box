package net.okocraft.box.migrator.database;

import net.okocraft.box.migrator.BoxMigrator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/*
 * source:
 * https://github.com/okocraft/Box/blob/v3/master/src/main/java/net/okocraft/box/database/Database.java
 */
public interface Database {

    @NotNull Connection getConnection() throws SQLException;

    default void execute(@NotNull String sql, @NotNull ResultSetConsumer resultSetConsumer) {
        try (var con = getConnection(); var preparedStatement = con.prepareStatement(sql)) {
            resultSetConsumer.accept(preparedStatement.executeQuery());
        } catch (Throwable e) {
            JavaPlugin.getPlugin(BoxMigrator.class)
                    .getLogger()
                    .log(Level.SEVERE, "Error occurred on executing sql: " + sql, e);
        }
    }

    void close();

    interface ResultSetConsumer {

        void accept(@NotNull ResultSet resultSet) throws Exception;

    }
}
