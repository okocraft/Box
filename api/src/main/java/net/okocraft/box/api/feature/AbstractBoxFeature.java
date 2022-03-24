package net.okocraft.box.api.feature;

import com.github.siroshun09.event4j.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An abstract implementation of {@link BoxFeature}.
 */
public abstract class AbstractBoxFeature implements BoxFeature {

    private final String name;
    private final Key listenerKey;

    /**
     * The constructor of {@link AbstractBoxFeature}.
     *
     * @param name the feature name
     */
    protected AbstractBoxFeature(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
        this.listenerKey = Key.create(name);
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Key getListenerKey() {
        return listenerKey;
    }
}
