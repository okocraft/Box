package net.okocraft.box.storage.implementation.yaml;

import net.okocraft.box.storage.api.model.AbstractStorage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class YamlStorage extends AbstractStorage {

    public static final String STORAGE_NAME = "Yaml";

    private final Path rootDirectory;

    public YamlStorage(@NotNull Path rootDirectory) {
        super(
                STORAGE_NAME,
                new YamlUserStorage(rootDirectory),
                new YamlItemStorage(rootDirectory),
                new YamlStockStorage(rootDirectory),
                new YamlCustomDataStorage(rootDirectory)
        );

        this.rootDirectory = rootDirectory;
    }

    @Override
    protected void initStorage() throws Exception {
        Files.createDirectories(rootDirectory);
    }

    @Override
    protected void closeStorage() {
    }
}
