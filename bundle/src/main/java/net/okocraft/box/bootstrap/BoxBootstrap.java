package net.okocraft.box.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.bundle.Builtin;
import net.okocraft.box.core.message.CoreMessages;
import net.okocraft.box.plugin.BoxPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.SubstituteLogger;

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
        this.boxBootstrapContext = BoxBootstrapContext.create(context);
        BoxBootstrap.instance = this;
        ((SubstituteLogger) BoxLogger.logger()).setDelegate(context.getLogger());

        CoreMessages.addDefaultMessages(this.boxBootstrapContext.getDefaultMessageCollector());
        Builtin.features(this.boxBootstrapContext);
        Builtin.storages(this.boxBootstrapContext.getStorageRegistry());
        Builtin.japaneseFile(this.boxBootstrapContext);
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        BoxBootstrap.instance = null;
        return new BoxPlugin(this.boxBootstrapContext);
    }

    public @NotNull BoxBootstrapContext getContext() { // This method exists for external access.
        return this.boxBootstrapContext;
    }
}
