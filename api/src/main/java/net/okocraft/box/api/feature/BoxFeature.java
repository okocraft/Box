package net.okocraft.box.api.feature;

import com.github.siroshun09.event4j.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Set;

/**
 * An interface that adds a box feature.
 */
public interface BoxFeature {

    /**
     * Gets the name of this feature.
     *
     * @return the name of this feature
     */
    @NotNull String getName();

    /**
     * Gets the key of listeners.
     *
     * @return the key of listeners
     */
    @NotNull Key getListenerKey();

    /**
     * Enables this feature.
     *
     * @deprecated use/override {@link #enable(FeatureContext.Enabling)}
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default void enable() {
    }

    /**
     * Enables this {@link BoxFeature}.
     *
     * @param context the {@link net.okocraft.box.api.feature.FeatureContext.Enabling} context
     * @throws Throwable if an exception occurred while enabling
     */
    default void enable(@NotNull FeatureContext.Enabling context) throws Throwable {
        this.enable();
    }

    /**
     * Disables this feature.
     * <p>
     * This method will be called even if an exception is thrown while executing {@link #enable()}.
     * @deprecated use/override {@link #disable(FeatureContext.Disabling)}
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default void disable() {
    }

    /**
     * Disables this {@link BoxFeature}.
     *
     * @param context the {@link net.okocraft.box.api.feature.FeatureContext.Disabling} context
     * @throws Throwable if an exception occurred while disabling
     */
    default void disable(@NotNull FeatureContext.Disabling context) throws Throwable {
        this.disable();
    }

    /**
     * Gets classes of the dependent {@link BoxFeature}.
     *
     * @return classes of the dependent {@link BoxFeature}
     */
    default @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Collections.emptySet();
    }
}
