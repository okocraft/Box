package net.okocraft.box.plugin.locale;

import com.github.siroshun09.configapi.bukkit.BukkitYaml;
import com.github.siroshun09.configapi.common.Configuration;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.locale.formatter.Formatter;
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
        Path filePath = plugin.getDataFolder().toPath().resolve(fileName);

        if (Files.exists(filePath)) {
            BukkitYaml yaml = new BukkitYaml(filePath);

            if (!yaml.isLoaded()) {
                plugin.getLogger().warning("Failed to load " + fileName + ", so we use default message....");
                return getDefault(plugin);
            } else {
                return new LocaleLoader(yaml);
            }
        } else {
            plugin.getLogger().warning("Could not find " + fileName + ", so we create a default language file...");

            BukkitYaml yaml = new BukkitYaml(filePath);

            saveDefault(yaml);

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
            saveDefault(yaml);
        }

        return new LocaleLoader(yaml);
    }

    private static void saveDefault(@NotNull BukkitYaml yaml) {
        for (Message message : Message.values()) {
            yaml.set(message.getPath(), message.getDefault());
        }

        yaml.save();
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

    @NotNull
    public String format(@NotNull Message message, @NotNull String holder1) {
        return Formatter.format(get(message), holder1);
    }

    @NotNull
    public String format(@NotNull Message message, @NotNull String holder1, @NotNull String holder2) {
        return Formatter.format(get(message), holder1, holder2);
    }

    @NotNull
    public String format(@NotNull Message message, @NotNull String holder1, @NotNull String holder2, @NotNull String holder3) {
        return Formatter.format(get(message), holder1, holder2, holder3);
    }

    @NotNull
    public String format(@NotNull Message message, @NotNull String... holders) {
        return Formatter.format(get(message), holders);
    }

    @NotNull
    public String replace(@NotNull Message message, int index, @NotNull String replacement) {
        return Formatter.replace(get(message), index, replacement);
    }
}
