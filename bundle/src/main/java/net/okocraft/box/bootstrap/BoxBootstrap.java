package net.okocraft.box.bootstrap;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import net.okocraft.box.bundle.BuiltinFeatures;
import net.okocraft.box.bundle.BuiltinStorages;
import net.okocraft.box.bundle.BuiltinTranslations;
import net.okocraft.box.plugin.BoxPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class BoxBootstrap implements PluginBootstrap {

    private static BoxBootstrap instance = null;

    public static @NotNull BoxBootstrap get() { // This method exists for external access.
        if (instance == null) {
            throw new IllegalStateException("There is no active BoxBootstrap.");
        }

        return instance;
    }

    @MonotonicNonNull
    private BootstrapContext bootstrapContext;

    @Override
    public void bootstrap(@NotNull PluginProviderContext context) {
        BoxBootstrap.instance = this;
        bootstrapContext = BootstrapContext.create(context);

        BuiltinFeatures.addToContext(bootstrapContext);
        BuiltinStorages.addToRegistry(bootstrapContext.getStorageRegistry());

        bootstrapContext.onLanguageDirectoryCreated().add(directory -> BuiltinTranslations.saveDefaultTranslationFiles(bootstrapContext.getJarFile(), directory));
        bootstrapContext.getTranslationLoaderCreators().addCreator(locale -> BuiltinTranslations.loadDefaultTranslation(bootstrapContext.getJarFile(), locale));
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) { // PluginProviderContext is immutable, so this argument can be ignored.
        BoxBootstrap.instance = null;
        return new BoxPlugin(bootstrapContext);
    }

    public @NotNull BootstrapContext getBootstrapContext() { // This method exists for external access.
        return bootstrapContext;
    }
}
