package net.okocraft.box.core.feature;

import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class BoxFeatureProvider implements FeatureProvider {

    private final Map<Class<? extends BoxFeature>, ? extends BoxFeature> featureMap;

    public BoxFeatureProvider(@NotNull Map<Class<? extends BoxFeature>, ? extends BoxFeature> featureMap) {
        this.featureMap = featureMap;
    }

    @Override
    public @NotNull <F extends BoxFeature> Optional<F> getFeature(@NotNull Class<? extends F> clazz) {
        return Optional.ofNullable(this.featureMap.get(clazz)).map(clazz::cast);
    }

    @Override
    public @NotNull @Unmodifiable Collection<? extends BoxFeature> getFeatures() {
        return Collections.unmodifiableCollection(this.featureMap.values());
    }
}
