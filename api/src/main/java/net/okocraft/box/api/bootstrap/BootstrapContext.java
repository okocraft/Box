package net.okocraft.box.api.bootstrap;

import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.model.manager.EventManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.function.Function;

public interface BootstrapContext {

    static @NotNull BootstrapContext get() {
        try {
            var clazz = Class.forName("net.okocraft.box.bootstrap.BoxBootstrap");
            var bootstrap = clazz.getMethod("get").invoke(null);
            var getContext = clazz.getMethod("getContext");
            return (BootstrapContext) getContext.invoke(bootstrap);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Could not get BootstrapContext", e);
        }
    }

    @NotNull Path getDataDirectory();

    @NotNull String getVersion();

    @NotNull EventManager getEventManager();

    @NotNull DefaultMessageCollector getDefaultMessageCollector();

    @Contract("_ -> this")
    @NotNull BootstrapContext addFeature(@NotNull Function<FeatureContext.Registration, ? extends BoxFeature> featureFactory);

}
