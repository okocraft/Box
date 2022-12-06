package net.okocraft.box.feature.category.internal.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public record MCDataVersion(int dataVersion) {
    @SuppressWarnings("deprecation")
    public static final MCDataVersion CURRENT = new MCDataVersion(Bukkit.getUnsafe().getDataVersion());

    public static final MCDataVersion MC_1_17_1 = new MCDataVersion(2730);
    public static final MCDataVersion MC_1_18_2 = new MCDataVersion(2975);
    public static final MCDataVersion MC_1_19_2 = new MCDataVersion(3120);
    public static final MCDataVersion MC_1_19_3 = new MCDataVersion(3217); // Not yet finalized - 1.19.3-rc2: 3216

    public boolean isBefore(@NotNull MCDataVersion other) {
        return dataVersion < other.dataVersion;
    }

    public boolean isAfter(@NotNull MCDataVersion other) {
        return dataVersion > other.dataVersion;
    }

    public boolean isSame(@NotNull MCDataVersion other) {
        return dataVersion == other.dataVersion();
    }

    public boolean isBeforeOrSame(@NotNull MCDataVersion other) {
        return isBefore(other) || isSame(other);
    }

    public boolean isAfterOrSame(@NotNull MCDataVersion other) {
        return isAfter(other) || isSame(other);
    }
}
