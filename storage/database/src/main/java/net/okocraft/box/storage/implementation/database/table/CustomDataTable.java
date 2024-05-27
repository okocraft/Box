package net.okocraft.box.storage.implementation.database.table;

import com.github.siroshun09.configapi.core.file.java.binary.BinaryFormat;
import com.github.siroshun09.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;

// | key | data |
public class CustomDataTable extends AbstractCustomDataTable {

    private final MetaTable metaTable;
    private LegacyCustomDataTable legacyCustomDataTable;

    public CustomDataTable(@NotNull Database database, @NotNull MetaTable metaTable) {
        super(database, database.getSchemaSet().customDataTable());
        this.metaTable = metaTable;
    }

    @Override
    public void init() throws Exception {
        this.createTableAndIndex();

        if (!this.metaTable.isCurrentCustomDataFormat() && this.legacyTableExists()) {
            this.legacyCustomDataTable = new LegacyCustomDataTable(this.database, false);
            this.legacyCustomDataTable.init();
        }
    }

    @Override
    public void updateFormatIfNeeded() throws Exception {
        if (this.legacyCustomDataTable != null) {
            this.legacyCustomDataTable.visitAllData((key, mapNode) -> {
                try {
                    this.saveData0(key, mapNode);
                } catch (Exception e) {
                    sneakyThrow(e);
                }
            });
            this.legacyCustomDataTable = null;
            this.metaTable.saveCurrentCustomDataFormat();
        }
    }

    @Override
    public @NotNull MapNode loadData(@NotNull Key key) throws Exception {
        return this.legacyCustomDataTable == null ? super.loadData(key) : this.legacyCustomDataTable.loadData(key);
    }

    @Override
    public void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception {
        if (this.legacyCustomDataTable == null) {
            super.saveData(key, mapNode);
        } else {
            this.legacyCustomDataTable.saveData(key, mapNode);
        }
    }

    private void saveData0(@NotNull Key key, @NotNull MapNode mapNode) throws Exception {
        super.saveData(key, mapNode);
    }

    @Override
    public void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        if (this.legacyCustomDataTable == null) {
            super.visitData(namespace, consumer);
        } else {
            this.legacyCustomDataTable.visitData(namespace, consumer);
        }
    }

    @Override
    public void visitAllData(@NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        if (this.legacyCustomDataTable == null) {
            super.visitAllData(consumer);
        } else {
            this.legacyCustomDataTable.visitAllData(consumer);
        }
    }

    @Override
    protected byte @NotNull [] toBytes(@NotNull MapNode node) throws Exception {
        try (var out = new ByteArrayOutputStream()) {
            BinaryFormat.DEFAULT.save(node, out);
            return out.toByteArray();
        }
    }

    protected @NotNull MapNode readDataFromResultSet(@NotNull ResultSet resultSet) throws Exception {
        try (var in = new ByteArrayInputStream(this.readBytesFromResultSet(resultSet, "data"))) {
            return BinaryFormat.DEFAULT.load(in) instanceof MapNode mapNode ? mapNode : MapNode.create();
        }
    }

    private boolean legacyTableExists() throws SQLException {
        try (var connection = this.database.getConnection()) {
            var metaData = connection.getMetaData();
            var tableName = this.database.getSchemaSet().legacyCustomDataTable().tableName();
            try (var resultSet = metaData.getTables(null, null, tableName, null)) {
                return resultSet.next();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(@NotNull Throwable exception) throws T {
        throw (T) exception;
    }
}
