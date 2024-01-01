package net.okocraft.box.api.feature;

import org.jetbrains.annotations.ApiStatus;

/**
 * An interface to indicate that the {@link BoxFeature} can be disabled in config.yml.
 *
 * @deprecated Disabling features by config will be no longer supported in Box v6
 */
@Deprecated(since = "5.5.2", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
public interface Disableable {
}
