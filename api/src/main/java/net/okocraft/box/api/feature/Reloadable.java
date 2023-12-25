package net.okocraft.box.api.feature;

import org.jetbrains.annotations.NotNull;

/**
 * an interface to indicate that it is reloadable.
 */
public interface Reloadable {

    /**
     * Reloads the {@link BoxFeature}.
     *
     * @param context the {@link net.okocraft.box.api.feature.FeatureContext.Reloading} context
     * @throws Throwable if an exception occurred while reloading
     */
    void reload(@NotNull FeatureContext.Reloading context) throws Throwable;

}
