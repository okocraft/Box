package net.okocraft.box.api.model.version;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A record that includes the version and {@link BuildData}
 *
 * @param version the version of Box
 * @param isReleaseVersion whether the current version is a released version or not
 * @param buildData the {@link BuildData}
 */
@ApiStatus.Experimental
public record VersionInfo(@NotNull String version,
                          boolean isReleaseVersion,
                          @NotNull BuildData buildData) {

    /**
     * A {@link String} indicating that the value could not be obtained.
     */
    public static final String UNKNOWN_VALUE = "UNKNOWN";

    /**
     * Checks if the given {@link String} is {@link #UNKNOWN_VALUE}.
     *
     * @param value the {@link String} to check
     * @return {@code true} if the given {@link String} is {@link #UNKNOWN_VALUE}, otherwise {@code false}
     */
    public static boolean isUnknown(@NotNull String value) {
        return value.equals(UNKNOWN_VALUE);
    }
}
