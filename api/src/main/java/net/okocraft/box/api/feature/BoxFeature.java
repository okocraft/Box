package net.okocraft.box.api.feature;

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
     * Enables this {@link BoxFeature}.
     *
     * @param context the {@link net.okocraft.box.api.feature.FeatureContext.Enabling} context
     * @throws Throwable if an exception occurred while enabling
     */
    void enable(@NotNull FeatureContext.Enabling context) throws Throwable;

    /**
     * Disables this {@link BoxFeature}.
     *
     * @param context the {@link net.okocraft.box.api.feature.FeatureContext.Disabling} context
     * @throws Throwable if an exception occurred while disabling
     */
    void disable(@NotNull FeatureContext.Disabling context) throws Throwable;

    /**
     * Gets classes of the dependent {@link BoxFeature}.
     *
     * @return classes of the dependent {@link BoxFeature}
     */
    default @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Collections.emptySet();
    }
}
