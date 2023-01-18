package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Predicate;

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

        try (var yaml = YamlConfiguration.create(file, configuration)) {
            yaml.save();
        }
    }

    @Override
    public @NotNull Collection<Key> getKeys() throws Exception {
        try (var listStream = Files.list(customDataDirectory)) {
            return listStream
                    .filter(Files::isDirectory)
                    .map(this::processDir)
                    .flatMap(Collection::stream)
                    .toList();
        }
    }

    private @NotNull Collection<Key> processDir(@NotNull Path dir) {
        try (var listStream = Files.list(dir)) {
            var dirName = dir.getFileName().toString();
            return listStream.filter(Files::isReadable)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(file -> file.endsWith(".yml"))
                    .map(file -> file.substring(0, file.length() - 4))
                    .filter(Predicate.not(String::isEmpty))
                    .map(key -> new Key(dirName, key))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
