package net.okocraft.box.api.feature;

import org.jetbrains.annotations.NotNull;

public interface FeatureFactory {

    @NotNull BoxFeature create(@NotNull FeatureContext.Registration context);

}
