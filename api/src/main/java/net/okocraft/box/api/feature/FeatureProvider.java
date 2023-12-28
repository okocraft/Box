package net.okocraft.box.api.feature;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;

/**
 * An interface to provide {@link BoxFeature}s.
 */
public interface FeatureProvider {

    /**
     * Gets the {@link BoxFeature} instance of the specified class.
     *
     * @param clazz the class to get
     * @param <F>   the {@link BoxFeature} type
     * @return if the {@link BoxFeature} is registered, returns its instance, otherwise {@link Optional#empty()}
     */
    <F extends BoxFeature> @NotNull Optional<F> getFeature(@NotNull Class<? extends F> clazz);

    /**
     * Gets all registered {@link BoxFeature}s.
     *
     * @return all registered {@link BoxFeature}s
     */
    @NotNull @Unmodifiable Collection<? extends BoxFeature> getFeatures();

}
