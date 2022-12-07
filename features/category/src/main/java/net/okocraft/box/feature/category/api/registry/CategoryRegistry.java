package net.okocraft.box.feature.category.api.registry;

import net.okocraft.box.feature.category.api.category.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CategoryRegistry {

    static @NotNull CategoryRegistry get() {
        return CategoryRegistryGetter.getRegistry();
    }

    void register(@NotNull String name, @NotNull Category category);

    void unregister(@NotNull String name);

    void unregister(@NotNull Category category);

    void unregisterAll();

    @NotNull Optional<Category> getByName(@NotNull String name);

    @NotNull String getRegisteredName(@NotNull Category category);

    @NotNull @Unmodifiable List<String> names();

    @NotNull @Unmodifiable List<Category> values();

    @NotNull @Unmodifiable Map<String, Category> asMap();
}
