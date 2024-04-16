package net.okocraft.box.feature.category.internal.registry;

import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.category.CustomItemCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CategoryRegistryImpl implements CategoryRegistry {

    private final Map<String, Category> registry = new LinkedHashMap<>();
    private final CustomItemCategory customItemCategory = new CustomItemCategory();
    private final Object lock = new Object();

    private List<Category> snapshot = Collections.emptyList();

    @Override
    public void register(@NotNull String name, @NotNull Category category) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(category);

        if (name.equals(CustomItemCategory.REGISTRY_KEY) && category != this.customItemCategory) {
            throw new IllegalArgumentException("Cannot register an unknown custom item category.");
        }

        synchronized (lock) {
            registry.put(name, category);
            snapshot = List.copyOf(registry.values());
        }
    }

    @Override
    public void unregister(@NotNull String name) {
        Objects.requireNonNull(name);

        synchronized (lock) {
            registry.remove(name);
            snapshot = List.copyOf(registry.values());
        }
    }

    @Override
    public void unregister(@NotNull Category category) {
        Objects.requireNonNull(category);

        synchronized (lock) {
            registry.values().removeIf(value -> value == category); // Removes only the specified instance.
            snapshot = List.copyOf(registry.values());
        }
    }

    @Override
    public void unregisterAll() {
        synchronized (lock) {
            registry.clear();
            snapshot = Collections.emptyList();
        }
    }

    @Override
    public @NotNull Optional<Category> getByName(@NotNull String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(registry.get(name));
    }

    @Override
    public @NotNull String getRegisteredName(@NotNull Category category) {
        Objects.requireNonNull(category);
        Map<String, Category> snapshot;

        synchronized (lock) {
            snapshot = Map.copyOf(registry);
        }

        for (var entry : snapshot.entrySet()) {
            if (entry.getValue() == category) {
                return entry.getKey();
            }
        }

        throw new IllegalArgumentException(category + " is not registered.");
    }

    @Override
    public @NotNull @Unmodifiable List<String> names() {
        List<String> names;

        synchronized (lock) {
            names = List.copyOf(registry.keySet());
        }

        return names;
    }

    @Override
    public @NotNull @Unmodifiable List<Category> values() {
        List<Category> result;

        synchronized (lock) {
            result = snapshot;
        }

        return result;
    }

    @Override
    public @NotNull @Unmodifiable Map<String, Category> asMap() {
        Map<String, Category> categoryMap;

        synchronized (lock) {
            categoryMap = new LinkedHashMap<>(this.registry);
        }

        return Collections.unmodifiableMap(categoryMap);
    }

    @Override
    public @NotNull CustomItemCategory getCustomItemCategory() {
        return this.customItemCategory;
    }
}
