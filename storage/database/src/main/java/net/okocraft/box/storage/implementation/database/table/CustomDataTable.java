package net.okocraft.box.storage.implementation.database.table;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.MappedConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;

// | key | data |
public class CustomDataTable extends AbstractTable implements CustomDataStorage {

    private static final Supplier<Yaml> YAML_SUPPLIER;

    static {
        var options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        var representer = new Representer();
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        YAML_SUPPLIER = () -> new Yaml(representer, options);
    }

    private final ThreadLocal<Yaml> yamlThreadLocal;

    public CustomDataTable(@NotNull Database database) {
        super(database, database.getSchemaSet().customDataTable());
        yamlThreadLocal = ThreadLocal.withInitial(YAML_SUPPLIER);
    }

    @Override
    public void init() throws Exception {
        createTableAndIndex();
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public @NotNull Configuration load(@NotNull String namespace, @NotNull String key) throws Exception {
        var namespacedKey = namespace + ":" + key;

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT data FROM `%table%` WHERE key=? LIMIT 1")) {
            statement.setString(1, namespacedKey);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    var blob = resultSet.getBlob("data");
                    return deserializeConfiguration(blob.getBytes(1, (int) blob.length()));
                }
            }
        }

        return MappedConfiguration.create();
    }

    @Override
    public void save(@NotNull String namespace, @NotNull String key, @NotNull Configuration configuration) throws Exception {
        var namespacedKey = namespace + ":" + key;

        try (var connection = database.getConnection()) {
            var data = serializeConfiguration(configuration);

            if (isExistingKey(connection, namespacedKey)) {
                updateData(connection, namespacedKey, data);
            } else {
                insertData(connection, namespacedKey, data);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private @NotNull Configuration deserializeConfiguration(byte[] bytes) {
        try (var input = new ByteArrayInputStream(bytes);
             var reader = new InputStreamReader(input)) {
                var map = yamlThreadLocal.get().loadAs(reader, LinkedHashMap.class);
                return MappedConfiguration.create(map);
        } catch (IOException e) {
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not deserialize data", e);
            return MappedConfiguration.create();
        }
    }

    private boolean isExistingKey(@NotNull Connection connection, @NotNull String namespacedKey) throws SQLException {
        try (var statement = prepareStatement(connection, "SELECT key FROM `%table%` WHERE key=? LIMIT 1")) {
            statement.setString(1, namespacedKey);

            try (var result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private byte[] serializeConfiguration(@NotNull Configuration config) {
        try (var byteOut = new ByteArrayOutputStream();
             var writer = new OutputStreamWriter(byteOut)) {
            var map = new LinkedHashMap<>();

            toMap(config, map, "");

            yamlThreadLocal.get().dump(map, writer);

            return byteOut.toByteArray();
        } catch (IOException e) {
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not serialize config", e);
            return new byte[0];
        }
    }

    private void toMap(@NotNull Configuration config, @NotNull Map<Object, Object> map, @NotNull String keyPrefix) {
        for (var key : config.getKeyList()) {
            var section = config.getSection(key);

            if (section != null) {
                var newKeyPrefix = keyPrefix + key + Configuration.PATH_SEPARATOR;
                toMap(section, map, newKeyPrefix);
                continue;
            }

            var obj = config.get(key);

            if (obj != null) {
                map.put(keyPrefix + key, obj);
            }
        }
    }

    private void insertData(@NotNull Connection connection, @NotNull String namespacedKey, byte[] data) throws SQLException {
        try (var statement = prepareStatement(connection, "INSERT INTO `%table%` (key, data) VALUES(?,?)")) {
            var blob = connection.createBlob();
            blob.setBytes(1, data);

            statement.setString(1, namespacedKey);
            statement.setBlob(2, blob);

            statement.execute();
        }
    }

    private void updateData(@NotNull Connection connection, @NotNull String namespacedKey, byte[] data) throws SQLException {
        try (var statement = prepareStatement(connection, "UPDATE `%table%` SET data=? where key=?")) {
            var blob = connection.createBlob();
            blob.setBytes(1, data);

            statement.setBlob(1, blob);
            statement.setString(2, namespacedKey);

            statement.execute();
        }
    }
}
