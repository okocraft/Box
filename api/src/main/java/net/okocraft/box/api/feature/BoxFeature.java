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
     * Enables this feature.
     */
    @Deprecated
    default void enable() {
    }

    default void enable(@NotNull FeatureContext.Enabling context) throws Throwable {
        this.enable();
    }

    /**
     * Disables this feature.
     * <p>
     * This method will be called even if an exception is thrown while executing {@link #enable()}.
     */
    @Deprecated
    default void disable() {
    }

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
