package net.okocraft.box.api.feature;

import org.jetbrains.annotations.NotNull;

/**
 * An interface to create the {@link BoxFeature}.
 */
public interface FeatureFactory {

    /**
     * Creates a new {@link BoxFeature}.
     *
     * @param context a {@link net.okocraft.box.api.feature.FeatureContext.Registration} context
     * @return a new {@link BoxFeature}
     */
    @NotNull BoxFeature create(@NotNull FeatureContext.Registration context);

}
