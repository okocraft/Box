package net.okocraft.box.api.event.feature;

import com.github.siroshun09.event4j.event.Event;
import net.okocraft.box.api.feature.BoxFeature;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a {@link BoxFeature} related event.
 */
public class FeatureEvent extends Event {

    private final BoxFeature feature;
    private final Type type;

    /**
     * The constructor of a {@link FeatureEvent}.
     *
     * @param feature the {@link BoxFeature} of this event
     * @param type    the type of this event
     */
    public FeatureEvent(@NotNull BoxFeature feature, @NotNull Type type) {
        this.feature = feature;
        this.type = type;
    }

    /**
     * Gets the {@link BoxFeature} of this event.
     *
     * @return the {@link BoxFeature} of this event
     */
    public @NotNull BoxFeature getFeature() {
        return feature;
    }

    /**
     * Gets the {@link Type} of this event.
     *
     * @return the {@link Type} of this event
     */
    public @NotNull Type getType() {
        return type;
    }

    /**
     * Types of this event.
     */
    public enum Type {

        /**
         * The feature was registered
         */
        REGISTER,

        /**
         * The feature was unregistered
         */
        UNREGISTER,

        /**
         * The feature was reloaded
         */
        RELOAD
    }
}
