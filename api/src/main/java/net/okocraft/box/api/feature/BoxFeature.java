package net.okocraft.box.api.feature;

import org.jetbrains.annotations.NotNull;

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
    void enable();

    /**
     * Disables this feature.
     * <p>
     * This method will be called even if an exception is thrown while executing {@link #enable()}.
     */
    void disable();
}
