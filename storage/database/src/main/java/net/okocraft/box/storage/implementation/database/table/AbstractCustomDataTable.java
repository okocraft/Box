package net.okocraft.box.storage.implementation.database.table;

import com.github.siroshun09.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.util.SneakyThrow;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.operator.CustomDataTableOperator;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public abstract class AbstractCustomDataTable implements CustomDataStorage {

    protected final Database database;
    protected final CustomDataTableOperator operator;

    public AbstractCustomDataTable(@NotNull Database database, @NotNull CustomDataTableOperator operator) {
        this.database = database;
        this.operator = operator;
    }

    @Override
    public @NotNull MapNode loadData(@NotNull Key key) throws Exception {
        byte[] data;

        try (var connection = this.database.getConnection()) {
            data = this.operator.selectDataByKey(connection, key.asString());
        }

        return data != null ? this.fromBytes(data) : MapNode.create();
    }

    @Override
    public void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception {
        try (var connection = this.database.getConnection()) {
            if (mapNode.value().isEmpty()) {
                this.operator.deleteData(connection, key.asString());
            } else {
                this.operator.upsertData(connection, key.asString(), this.toBytes(mapNode));
            }
        }
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public void visitData(@NotNull @KeyPattern.Namespace String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        if (!Key.parseableNamespace(namespace)) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }

        try (var connection = this.database.getConnection()) {
            this.operator.selectDataByNamespace(connection, namespace, (key, data) -> {
                try {
                    consumer.accept(Key.key(key), this.fromBytes(data));
                } catch (Exception e) {
                    SneakyThrow.sneaky(e);
                }
            });
        }
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public void visitAllData(@NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        try (var connection = this.database.getConnection()) {
            this.operator.selectAllData(connection, (key, data) -> {
                try {
                    consumer.accept(Key.key(key), this.fromBytes(data));
                } catch (Exception e) {
                    SneakyThrow.sneaky(e);
                }
            });
        }
    }

    protected abstract @NotNull MapNode fromBytes(byte[] data) throws Exception;

    protected abstract byte @NotNull [] toBytes(@NotNull MapNode node) throws Exception;

}
