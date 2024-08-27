package net.okocraft.box.storage.implementation.database.table;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.operator.RemappedItemTableOperator;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class RemappedItemTable implements RemappedItemStorage {

    private final Database database;
    private final RemappedItemTableOperator operator;

    public RemappedItemTable(@NotNull Database database) {
        this.database = database;
        this.operator = database.operators().remappedItemTable();
    }

    public void init(@NotNull Connection connection) throws SQLException {
        this.operator.initTable(connection);
    }

    @Override
    public @NotNull Map<MCDataVersion, Int2IntMap> loadRemappedIds() throws Exception {
        var result = new Object2ObjectOpenHashMap<MCDataVersion, Int2IntMap>();

        try (var connection = this.database.getConnection()) {
            this.operator.selectAllRemappedIds(
                connection,
                (inVersion, item) ->
                    result.computeIfAbsent(MCDataVersion.of(inVersion), ignored -> new Int2IntOpenHashMap())
                        .put(item.oldId(), item.newId())
            );
        }

        return result;
    }

    @Override
    public @NotNull Int2IntMap loadRemappedIds(@NotNull MCDataVersion version) throws Exception {
        var result = new Int2IntOpenHashMap();

        try (var connection = this.database.getConnection()) {
            this.operator.selectAllRemappedIdsByVersion(connection, version.dataVersion(), item -> result.put(item.oldId(), item.newId()));
        }

        return result;
    }

    @Override
    public void saveRemappedItem(int id, @NotNull String name, int remappedTo, @NotNull MCDataVersion inVersion) throws Exception {
        try (var connection = this.database.getConnection()) {
            this.operator.insert(connection, id, name, remappedTo, inVersion.dataVersion());
        }
    }
}
