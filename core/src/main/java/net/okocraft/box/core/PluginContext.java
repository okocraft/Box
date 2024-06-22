package net.okocraft.box.core;

import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.core.command.CommandRegisterer;
import net.okocraft.box.core.config.Config;
import net.okocraft.box.core.message.BoxMessageProvider;
import net.okocraft.box.core.model.manager.event.BoxEventManager;
import net.okocraft.box.storage.api.model.item.provider.DefaultItemProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record PluginContext(@NotNull JavaPlugin plugin,
                            @NotNull String version,
                            @NotNull Path dataDirectory,
                            @NotNull BoxScheduler scheduler,
                            @NotNull BoxEventManager eventManager,
                            @NotNull BoxMessageProvider messageProvider,
                            @NotNull Config config,
                            @NotNull DefaultItemProvider defaultItemProvider,
                            @NotNull CommandRegisterer commandRegisterer) {
}
