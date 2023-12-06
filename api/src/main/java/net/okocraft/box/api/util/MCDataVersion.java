package net.okocraft.box.api.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A class to get/compare Minecraft versions based on data version.
 *
 * @param dataVersion the data version (<a href="https://minecraft.fandom.com/wiki/Data_version">Minecraft Wiki: Data version</a>)
 */
@SuppressWarnings("unused")
public record MCDataVersion(int dataVersion) {

    /**
     * A {@link MCDataVersion} that represents the version of the server on which Box is currently running.
     */
    @SuppressWarnings("deprecation")
    public static final MCDataVersion CURRENT = new MCDataVersion(Bukkit.getUnsafe().getDataVersion());

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.17
     */
    public static final MCDataVersion MC_1_17 = new MCDataVersion(2724);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.17.1
     */
    public static final MCDataVersion MC_1_17_1 = new MCDataVersion(2730);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.18
     */
    public static final MCDataVersion MC_1_18 = new MCDataVersion(2860);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.18.1
     */
    public static final MCDataVersion MC_1_18_1 = new MCDataVersion(2865);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.18.2
     */
    public static final MCDataVersion MC_1_18_2 = new MCDataVersion(2975);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19
     */
    public static final MCDataVersion MC_1_19 = new MCDataVersion(3105);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19.1
     */
    public static final MCDataVersion MC_1_19_1 = new MCDataVersion(3117);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19.2
     */
    public static final MCDataVersion MC_1_19_2 = new MCDataVersion(3120);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19.3
     */
    public static final MCDataVersion MC_1_19_3 = new MCDataVersion(3218);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19.4
     */
    public static final MCDataVersion MC_1_19_4 = new MCDataVersion(3337);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20
     */
    public static final MCDataVersion MC_1_20 = new MCDataVersion(3463);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.1
     */
    public static final MCDataVersion MC_1_20_1 = new MCDataVersion(3465);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.2
     */
    public static final MCDataVersion MC_1_20_2 = new MCDataVersion(3578);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.3
     */
    public static final MCDataVersion MC_1_20_3 = new MCDataVersion(3698);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21
     */
    public static final MCDataVersion MC_1_21 = new MCDataVersion(9999); // Unknown

    /**
     * Creates a {@link MCDataVersion} from the specified data version
     *
     * @param dataVersion the data version
     * @return a {@link MCDataVersion}
     */
    @Contract("_ -> new")
    public static @NotNull MCDataVersion of(int dataVersion) {
        return new MCDataVersion(dataVersion);
    }

    /**
     * Checks if this {@link MCDataVersion} is before the given {@link MCDataVersion}.
     *
     * @param other the {@link MCDataVersion} to compare
     * @return {@code true} if this {@link MCDataVersion} is before the given {@link MCDataVersion}, otherwise {@code false}
     */
    public boolean isBefore(@NotNull MCDataVersion other) {
        return dataVersion < other.dataVersion;
    }

    /**
     * Checks if this {@link MCDataVersion} is after the given {@link MCDataVersion}.
     *
     * @param other the {@link MCDataVersion} to compare
     * @return {@code true} if this {@link MCDataVersion} is after the given {@link MCDataVersion}, otherwise {@code false}
     */
    public boolean isAfter(@NotNull MCDataVersion other) {
        return dataVersion > other.dataVersion;
    }

    /**
     * Checks if this {@link MCDataVersion} is same as the given {@link MCDataVersion}.
     *
     * @param other the {@link MCDataVersion} to compare
     * @return {@code true} if this {@link MCDataVersion} is same as the given {@link MCDataVersion}, otherwise {@code false}
     */
    public boolean isSame(@NotNull MCDataVersion other) {
        return dataVersion == other.dataVersion();
    }

    /**
     * Checks if this {@link MCDataVersion} is the same as or earlier than the given {@link MCDataVersion}.
     *
     * @param other the {@link MCDataVersion} to compare
     * @return {@code true} if this {@link MCDataVersion} is the same as or earlier than the given {@link MCDataVersion}, otherwise {@code false}
     * @see #isBefore(MCDataVersion)
     * @see #isSame(MCDataVersion)
     */
    public boolean isBeforeOrSame(@NotNull MCDataVersion other) {
        return isBefore(other) || isSame(other);
    }

    /**
     * Checks if this {@link MCDataVersion} is the same as or later than the given {@link MCDataVersion}.
     *
     * @param other the {@link MCDataVersion} to compare
     * @return {@code true} if this {@link MCDataVersion} is the same as or later than the given {@link MCDataVersion}, otherwise {@code false}
     * @see #isAfter(MCDataVersion)
     * @see #isSame(MCDataVersion)
     */
    public boolean isAfterOrSame(@NotNull MCDataVersion other) {
        return isAfter(other) || isSame(other);
    }

    /**
     * Checks if this {@link MCDataVersion} is contained between the specified {@link MCDataVersion}s.
     *
     * @param startInclusive beginning of included {@link MCDataVersion}
     * @param endInclusive end of included {@link MCDataVersion}
     * @return {@code true} if this {@link MCDataVersion} is contained between the specified {@link MCDataVersion}s, otherwise {@code false}
     */
    public boolean isBetween(@NotNull MCDataVersion startInclusive, @NotNull MCDataVersion endInclusive) {
        return isAfterOrSame(startInclusive) && isBeforeOrSame(endInclusive);
    }
}
