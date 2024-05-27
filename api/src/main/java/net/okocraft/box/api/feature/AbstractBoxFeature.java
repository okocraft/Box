package net.okocraft.box.api.feature;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An abstract implementation of {@link BoxFeature}.
 */
public abstract class AbstractBoxFeature implements BoxFeature {

    private final String name;

    /**
     * The constructor of {@link AbstractBoxFeature}.
     *
     * @param name the feature name
     */
    protected AbstractBoxFeature(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

}
