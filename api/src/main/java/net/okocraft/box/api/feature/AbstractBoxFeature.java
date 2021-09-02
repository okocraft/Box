package net.okocraft.box.api.feature;

import com.github.siroshun09.event4j.handlerlist.Key;
import org.jetbrains.annotations.NotNull;

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
        this.name = name;
        this.listenerKey = Key.of(name);
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
