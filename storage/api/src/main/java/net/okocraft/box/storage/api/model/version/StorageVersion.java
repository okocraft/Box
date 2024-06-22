package net.okocraft.box.storage.api.model.version;

import net.okocraft.box.api.util.Version;
import org.jetbrains.annotations.NotNull;

public record StorageVersion(int value) implements Version<StorageVersion> {

    public static final StorageVersion BEFORE_V6 = new StorageVersion(Integer.MIN_VALUE);
    public static final StorageVersion V6 = new StorageVersion(1);

    public static @NotNull StorageVersion latest() {
        return V6;
    }

    @Override
    public int compareTo(@NotNull StorageVersion other) {
        return Integer.compare(this.value, other.value);
    }
}
