package net.okocraft.box.storage.implementation.database.table;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;

public class LegacyCustomDataTable extends AbstractCustomDataTable {

    private final boolean createTable;

    public LegacyCustomDataTable(@NotNull Database database, boolean createTable) {
        super(database, database.getSchemaSet().legacyCustomDataTable());
        this.createTable = createTable;
    }

    @Override
    public void init() throws Exception {
        if (this.createTable) {
            this.createTableAndIndex();
        }
    }

    @Override
    protected byte @NotNull [] toBytes(@NotNull MapNode node) throws Exception {
        try (var out = new ByteArrayOutputStream()) {
            YamlFormat.DEFAULT.save(node, out);
            return out.toByteArray();
        }
    }

    protected @NotNull MapNode readDataFromResultSet(@NotNull ResultSet resultSet) throws Exception {
        try (var in = new ByteArrayInputStream(readBytesFromResultSet(resultSet, "data"));
             var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return YamlFormat.DEFAULT.load(reader);
        }
    }
}
