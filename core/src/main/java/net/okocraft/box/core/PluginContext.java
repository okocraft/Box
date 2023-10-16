package net.okocraft.box.core;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.event4j.bus.EventBus;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public record PluginContext(@NotNull JavaPlugin plugin,
                            @NotNull String version,
                            @NotNull Path dataDirectory,
                            @NotNull Path jarFile,
                            @NotNull BoxScheduler scheduler,
                            @NotNull EventBus<BoxEvent> eventBus,
                            @NotNull YamlConfiguration configuration,
                            @NotNull TranslationDirectory translationDirectory,
                            @NotNull StorageRegistry storageRegistry) {
}
