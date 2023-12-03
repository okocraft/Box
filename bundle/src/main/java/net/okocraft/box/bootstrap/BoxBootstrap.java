package net.okocraft.box.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import net.okocraft.box.bundle.Builtin;
import net.okocraft.box.bundle.BuiltinTranslations;
import net.okocraft.box.plugin.BoxPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class BoxBootstrap implements PluginBootstrap {

    private static BoxBootstrap instance = null;

    public static @NotNull BoxBootstrap get() { // This method exists for external access.
        if (instance == null) {
            throw new IllegalStateException("There is no active BoxBootstrap.");
        }

        return instance;
    }

    @MonotonicNonNull
    private BoxBootstrapContext boxBootstrapContext;

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        BoxBootstrap.instance = this;
        boxBootstrapContext = BoxBootstrapContext.create(context);

        Builtin.features(this.boxBootstrapContext);
        Builtin.storages(this.boxBootstrapContext.getStorageRegistry());

        boxBootstrapContext.onLanguageDirectoryCreated().add(directory -> BuiltinTranslations.saveDefaultTranslationFiles(boxBootstrapContext.getJarFile(), directory));
        boxBootstrapContext.getTranslationLoaderCreators().addCreator(locale -> BuiltinTranslations.loadDefaultTranslation(boxBootstrapContext.getJarFile(), locale));
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) { // PluginProviderContext is immutable, so this argument can be ignored.
        BoxBootstrap.instance = null;
        return new BoxPlugin(boxBootstrapContext);
    }

    public @NotNull BoxBootstrapContext getBootstrapContext() { // This method exists for external access.
        return boxBootstrapContext;
    }
}
