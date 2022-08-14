package net.okocraft.box.feature.category.internal.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public record MCDataVersion(int finalDataVersion) {
    @SuppressWarnings("deprecation")
    public static final MCDataVersion CURRENT = new MCDataVersion(Bukkit.getUnsafe().getDataVersion());

    public static final MCDataVersion MC_1_17 = new MCDataVersion(2730);
    public static final MCDataVersion MC_1_18 = new MCDataVersion(2975);
    public static final MCDataVersion MC_1_19 = new MCDataVersion(3120);

    public boolean isBefore(@NotNull MCDataVersion other) {
        return finalDataVersion < other.finalDataVersion;
    }

    public boolean isAfter(@NotNull MCDataVersion other) {
        return finalDataVersion > other.finalDataVersion;
    }

    public boolean isSame(@NotNull MCDataVersion other) {
        return finalDataVersion == other.finalDataVersion();
    }

    public boolean isBeforeOrSame(@NotNull MCDataVersion other) {
        return isBefore(other) || isSame(other);
    }

    public boolean isAfterOrSame(@NotNull MCDataVersion other) {
        return isAfter(other) || isSame(other);
    }
}
