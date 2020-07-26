package net.okocraft.box.plugin.locale;

import com.github.siroshun09.configapi.bukkit.BukkitYaml;
import com.github.siroshun09.configapi.common.Configuration;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.locale.message.Message;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public class LocaleLoader {

    private final Configuration config;

    private LocaleLoader(@NotNull Configuration config) {
        this.config = config;
    }

    @NotNull
    public static LocaleLoader tryLoad(@NotNull Box plugin, @NotNull String fileName) {
        BukkitYaml yaml = new BukkitYaml(plugin.getDataFolder().toPath().resolve(fileName));

        if (!yaml.isLoaded()) {
            plugin.getLogger().warning("Failed to load " + fileName + ", so we use default message....");
            return getDefault(plugin);
        } else {
            return new LocaleLoader(yaml);
        }
    }

    @NotNull
    public static LocaleLoader getDefault(@NotNull Box plugin) {
        Path filePath = plugin.getDataFolder().toPath().resolve("lang.yml");

        BukkitYaml yaml;

        if (Files.exists(filePath)) {
            yaml = new BukkitYaml(filePath);
        } else {
            yaml = new BukkitYaml(filePath);

            for (Message message : Message.values()) {
                yaml.set(message.getPath(), message.getDefault());
            }

            yaml.save();
        }

        return new LocaleLoader(yaml);
    }

    @NotNull
    public String get(@NotNull Message message) {
        String msg = config.getString(message.getPath());

        if (msg.isEmpty()) {
            return message.getDefault();
        } else {
            return msg;
        }
    }
}
