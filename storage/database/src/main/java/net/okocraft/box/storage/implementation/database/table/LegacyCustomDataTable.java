package net.okocraft.box.storage.implementation.database.table;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.format.yaml.YamlFormat;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class LegacyCustomDataTable extends AbstractCustomDataTable {

    private final boolean createTable;

    public LegacyCustomDataTable(@NotNull Database database, boolean createTable) {
        super(database, database.operators().legacyCustomDataTable());
        this.createTable = createTable;
    }

    public void init(@NotNull Connection connection) throws Exception {
        if (this.createTable) {
            this.operator.initTable(connection);
        }
    }

    @Override
    protected @NotNull MapNode fromBytes(byte[] data) throws Exception {
        try (var in = new ByteArrayInputStream(data);
             var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return YamlFormat.DEFAULT.load(reader);
        }
    }

    @Override
    protected byte @NotNull [] toBytes(@NotNull MapNode node) throws Exception {
        try (var out = new ByteArrayOutputStream()) {
            YamlFormat.DEFAULT.save(node, out);
            return out.toByteArray();
        }
    }
}
