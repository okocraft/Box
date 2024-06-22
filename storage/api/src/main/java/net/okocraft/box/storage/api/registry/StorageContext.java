package net.okocraft.box.storage.api.registry;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record StorageContext<R>(@NotNull Path pluginDirectory, @NotNull R setting) {
}
