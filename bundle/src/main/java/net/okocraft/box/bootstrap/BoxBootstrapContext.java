package net.okocraft.box.bootstrap;

import com.github.siroshun09.messages.api.directory.DirectorySource;
import com.github.siroshun09.messages.api.util.Loader;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.feature.FeatureFactory;
import net.okocraft.box.core.message.BoxMessageProvider;
import net.okocraft.box.core.model.manager.event.BoxEventManager;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class BoxBootstrapContext implements net.okocraft.box.api.bootstrap.BootstrapContext {

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
    private final BoxEventManager eventManager;
    private final BoxMessageProvider.Collector defaultMessageCollector;
    private final Map<Locale, Loader<Locale, Map<String, String>>> localizationLoaderMap = new HashMap<>();
    private final List<BoxFeature> boxFeatureList = new ArrayList<>();
    private final FeatureContext.Registration featureRegistrationContext;

    @VisibleForTesting
    BoxBootstrapContext(@NotNull Path pluginDirectory, @NotNull String version) {
        this.dataDirectory = pluginDirectory;
        this.version = version;
        this.storageRegistry = new StorageRegistry();
        this.eventManager = BoxEventManager.create();
        this.defaultMessageCollector = BoxMessageProvider.createCollector();
        this.featureRegistrationContext = new FeatureContext.Registration(this.dataDirectory, this.defaultMessageCollector, this.eventManager);
    }

    @Override
    public @NotNull Path getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public @NotNull BoxEventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public @NotNull BoxMessageProvider.Collector getDefaultMessageCollector() {
        return this.defaultMessageCollector;
    }

    @Override
    @Contract("_ -> this")
    public @NotNull BoxBootstrapContext addFeature(@NotNull FeatureFactory factory) {
        this.boxFeatureList.add(factory.create(this.featureRegistrationContext));
        return this;
    }

    public @NotNull StorageRegistry getStorageRegistry() {
        return storageRegistry;
    }

    public @NotNull List<BoxFeature> getBoxFeatureList() {
        return this.boxFeatureList;
    }

    public void addLocalization(@NotNull Locale locale, @NotNull Supplier<Map<String, String>> defaultMessagesSupplier) {
        this.localizationLoaderMap.put(locale, ignored -> {
            try {
                return defaultMessagesSupplier.get();
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        });
    }

    public @NotNull BoxMessageProvider createMessageProvider() {
        return new BoxMessageProvider(
                this.defaultMessageCollector,
                DirectorySource.propertiesFiles(this.dataDirectory.resolve("languages")).defaultLocale(this.localizationLoaderMap.keySet()),
                locale -> {
                    var loader = this.localizationLoaderMap.get(locale);
                    return loader != null ? loader.load(locale) : null;
                }
        );
    }
}
