package net.okocraft.box.storage.api.registry;

import org.jetbrains.annotations.NotNull;

public record StorageContext<R>(@NotNull BaseStorageContext base, @NotNull R setting) {
}
