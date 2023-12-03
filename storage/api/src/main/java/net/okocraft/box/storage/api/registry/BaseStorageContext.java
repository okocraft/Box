package net.okocraft.box.storage.api.registry;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.logging.Logger;

public record BaseStorageContext(@NotNull Path rootDirectory, @NotNull Logger logger) {
}
