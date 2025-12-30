package net.okocraft.box.api.bootstrap;

import dev.siroshun.event4j.api.caller.EventCaller;
import dev.siroshun.event4j.api.listener.ListenerSubscriber;
import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureFactory;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
            Class<?> clazz = Class.forName("net.okocraft.box.bootstrap.BoxBootstrap");
            Object bootstrap = clazz.getMethod("get").invoke(null);
            Method getContext = clazz.getMethod("getContext");
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
     * Gets the {@link EventCaller}.
     *
     * @return the {@link EventCaller}
     */
    @NotNull EventCaller<BoxEvent> getEventCaller();

    /**
     * Gets the {@link ListenerSubscriber}.
     *
     * @return the {@link ListenerSubscriber}
     */
    @NotNull ListenerSubscriber<Key, BoxEvent, Priority> getListenerSubscriber();

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
