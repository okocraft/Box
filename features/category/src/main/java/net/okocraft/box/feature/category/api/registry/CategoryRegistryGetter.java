package net.okocraft.box.feature.category.api.registry;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.feature.category.CategoryFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

class CategoryRegistryGetter {

    static CategoryRegistry registry;

    static @NotNull CategoryRegistry getRegistry() {
        if (registry != null) {
            return registry;
        }

        Optional<CategoryFeature> feature = BoxAPI.api().getFeatureProvider().getFeature(CategoryFeature.class);

        if (feature.isEmpty()) {
            throw new IllegalStateException("category feature is not enabled.");
        }

        registry = feature.get().getCategoryRegistry();
        return registry;
    }
}
