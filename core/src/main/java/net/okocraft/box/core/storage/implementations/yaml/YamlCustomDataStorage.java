package net.okocraft.box.core.storage.implementations.yaml;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.core.storage.model.data.CustomDataStorage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class YamlCustomDataStorage implements CustomDataStorage {

    private final Path customDataDirectory;

    public YamlCustomDataStorage(@NotNull Path rootDirectory) {
        this.customDataDirectory = rootDirectory.resolve("custom-data");
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(customDataDirectory);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public @NotNull Configuration load(@NotNull String namespace, @NotNull String key) throws Exception {
        var file = customDataDirectory.resolve(namespace).resolve(key + ".yml");
        var yaml = YamlConfiguration.create(file);

        yaml.load();

        return yaml;
    }

    @Override
    public void save(@NotNull String namespace, @NotNull String key,
                     @NotNull Configuration configuration) throws Exception {
        var file = customDataDirectory.resolve(namespace).resolve(key + ".yml");
        var yaml = YamlConfiguration.create(file, configuration);

        yaml.save();
    }
}
