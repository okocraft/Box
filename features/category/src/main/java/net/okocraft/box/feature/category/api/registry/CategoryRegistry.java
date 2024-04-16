package net.okocraft.box.feature.category.api.registry;

import net.okocraft.box.feature.category.api.category.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A class for registry of {@link Category}.
 */
public interface CategoryRegistry {

    /**
     * Gets the {@link CategoryRegistry} instance.
     *
     * @return the {@link CategoryRegistry} instance
     */
    static @NotNull CategoryRegistry get() {
        return CategoryRegistryGetter.getRegistry();
    }

    /**
     * Registers a new {@link Category}.
     *
     * @param name     the name to register {@link Category}
     * @param category the {@link Category} to register
     */
    void register(@NotNull String name, @NotNull Category category);

    /**
     * Unregisters a {@link Category} by its registered name.
     *
     * @param name the name of {@link Category} to unregister
     */
    void unregister(@NotNull String name);

    /**
     * Unregisters a {@link Category}.
     * <p>
     * This method unregisters {@link Category} for the same instance as the argument.
     *
     * @param category the {@link Category} to unregister
     */
    void unregister(@NotNull Category category);

    /**
     * Unregisters all {@link Category}.
     */
    void unregisterAll();

    /**
     * Gets the {@link Category} that registered with the specified name.
     *
     * @param name the name to search for {@link Category}
     * @return the {@link Optional} that will have {@link Category}
     */
    @NotNull Optional<Category> getByName(@NotNull String name);

    /**
     * Gets the name of {@link Category}.
     *
     * @param category the {@link Category} to search for its name
     * @return the registered name
     * @throws IllegalStateException if the given {@link Category} is not registered to this registry
     */
    @NotNull String getRegisteredName(@NotNull Category category);

    /**
     * Gets the list of registered names.
     *
     * @return the list of registered names
     */
    @NotNull @Unmodifiable List<String> names();

    /**
     * Gets the list of registered {@link Category}.
     *
     * @return the list of registered {@link Category}
     */
    @NotNull @Unmodifiable List<Category> values();

    /**
     * Converts this {@link CategoryRegistry} to {@link Map}.
     *
     * @return the {@link Map} of this registry
     */
    @NotNull @Unmodifiable Map<String, Category> asMap();

    @NotNull Category getCustomItemCategory();

}
