package net.okocraft.box.api.feature;

import com.github.siroshun09.event4j.handlerlist.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Set;

/**
 * An interface that adds a box feature.
 */
public interface BoxFeature {

    /**
     * Gets the name of this feature.
     *
     * @return the name of this feature
     */
    @NotNull String getName();

    /**
     * Gets the key of listeners.
     *
     * @return the key of listeners
     */
    @NotNull Key getListenerKey();

    /**
     * Enables this feature.
     */
    void enable();

    /**
     * Disables this feature.
     * <p>
     * This method will be called even if an exception is thrown while executing {@link #enable()}.
     */
    void disable();

    /**
     * Gets classes of the dependent {@link BoxFeature}.
     *
     * @return classes of the dependent {@link BoxFeature}
     */
   default @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
       return Collections.emptySet();
   }
}
