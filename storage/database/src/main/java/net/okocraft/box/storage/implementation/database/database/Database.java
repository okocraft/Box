package net.okocraft.box.storage.implementation.database.database;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.implementation.database.operator.OperatorProvider;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface Database extends AutoCloseable {

    void prepare() throws Exception;

    void shutdown() throws Exception;

    String tablePrefix();

    @NotNull
    List<Storage.Property> getInfo();

    @NotNull
    Connection getConnection() throws SQLException;

    @NotNull
    OperatorProvider operators();

    @Override
    default void close() throws Exception {
        this.shutdown();
    }
}
