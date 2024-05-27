package net.okocraft.box.api.event.feature;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.feature.BoxFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class that represents a {@link BoxFeature} related event.
 */
public class FeatureEvent extends BoxEvent {

    private final BoxFeature feature;
    private final Type type;

    /**
     * The constructor of a {@link FeatureEvent}.
     *
     * @param feature the {@link BoxFeature} of this event
     * @param type    the type of this event
     */
    public FeatureEvent(@NotNull BoxFeature feature, @NotNull Type type) {
        this.feature = Objects.requireNonNull(feature);
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Gets the {@link BoxFeature} of this event.
     *
     * @return the {@link BoxFeature} of this event
     */
    public @NotNull BoxFeature getFeature() {
        return this.feature;
    }

    /**
     * Gets the {@link Type} of this event.
     *
     * @return the {@link Type} of this event
     */
    public @NotNull Type getType() {
        return this.type;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "FeatureEvent{" +
                "feature=" + this.feature.getName() +
                ", type=" + this.type +
                '}';
    }

    @Override
    public String toString() {
        return "FeatureEvent{" +
                "feature=" + this.feature +
                ", type=" + this.type +
                '}';
    }

    /**
     * Types of this event.
     */
    public enum Type {

        /**
         * The feature was enabled
         */
        ENABLE,

        /**
         * The feature was disabled
         */
        DISABLE,

        /**
         * The feature was reloaded
         */
        RELOAD
    }
}
