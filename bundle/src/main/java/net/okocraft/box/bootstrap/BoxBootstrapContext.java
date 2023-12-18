package net.okocraft.box.bootstrap;

import com.github.siroshun09.event4j.priority.Priority;
import com.github.siroshun09.event4j.simple.EventServiceProvider;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class BoxBootstrapContext {

    @Contract("_ -> new")
    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull BoxBootstrapContext create(@NotNull BootstrapContext context) {
        return new BoxBootstrapContext(
                context.getDataDirectory(),
                context.getConfiguration().getVersion()
        );
    }

    private final Path dataDirectory;
    private final String version;
    private final StorageRegistry storageRegistry;
    private final EventServiceProvider<Key, BoxEvent, Priority> eventServiceProvider;
    private final List<Supplier<? extends BoxFeature>> boxFeatureList = new ArrayList<>();

    private BoxBootstrapContext(@NotNull Path pluginDirectory, @NotNull String version) {
        this.dataDirectory = pluginDirectory;
        this.version = version;
        this.storageRegistry = new StorageRegistry();
        this.eventServiceProvider = EventServiceProvider.factory().keyClass(Key.class).eventClass(BoxEvent.class).orderComparator(Priority.COMPARATOR, Priority.NORMAL).create();
    }

    public @NotNull Path getPluginDirectory() {
        return dataDirectory;
    }

    public @NotNull String getVersion() {
        return version;
    }

    public @NotNull StorageRegistry getStorageRegistry() {
        return storageRegistry;
    }

    public @NotNull EventServiceProvider<Key, BoxEvent, Priority> getEventServiceProvider() {
        return this.eventServiceProvider;
    }

    public @NotNull List<Supplier<? extends BoxFeature>> getBoxFeatureList() {
        return boxFeatureList;
    }

    @Contract("_ -> this")
    public @NotNull BoxBootstrapContext addFeature(@NotNull Supplier<? extends BoxFeature> featureSupplier) {
        boxFeatureList.add(featureSupplier);
        return this;
    }
}
