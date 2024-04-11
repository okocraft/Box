package net.okocraft.box.api.bootstrap;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureFactory;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.model.manager.EventManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

/**
 * An interface that represents the context during Box initialization.
 * <p>
 * This context is only available before {@link net.okocraft.box.api.BoxAPI} is initialized.
 */
public interface BootstrapContext {

    /**
     * Gets the {@link BootstrapContext}.
     *
     * @return the {@link BootstrapContext}
     * @throws IllegalStateException if Box is not being initialized
     */
    static @NotNull BootstrapContext get() {
        try {
            var clazz = Class.forName("net.okocraft.box.bootstrap.BoxBootstrap");
            var bootstrap = clazz.getMethod("get").invoke(null);
            var getContext = clazz.getMethod("getContext");
            return (BootstrapContext) getContext.invoke(bootstrap);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Could not get BootstrapContext", e);
        }
    }

    /**
     * Gets the data directory of Box.
     *
     * @return the data directory of Box
     */
    @NotNull Path getDataDirectory();

    /**
     * Gets the version of Box.
     *
     * @return the version of Box
     */
    @NotNull String getVersion();

    /**
     * Gets the {@link EventManager}.
     * <p>
     * This {@link EventManager} is the same as {@link BoxAPI#getEventManager()},
     * and listeners registered to this remain active after initialization.
     *
     * @return the {@link EventManager}
     */
    @NotNull EventManager getEventManager();

    /**
     * Gets the {@link DefaultMessageCollector}.
     *
     * @return the {@link DefaultMessageCollector}
     */
    @NotNull DefaultMessageCollector getDefaultMessageCollector();

    /**
     * Adds a {@link FeatureFactory} to create and register {@link BoxFeature}.
     *
     * @param factory a {@link FeatureFactory} to create and register {@link BoxFeature}
     * @return this {@link BootstrapContext}
     */
    @Contract("_ -> this")
    @NotNull BootstrapContext addFeature(@NotNull FeatureFactory factory);

}
