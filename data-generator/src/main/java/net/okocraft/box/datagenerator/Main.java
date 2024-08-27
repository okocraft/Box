package net.okocraft.box.datagenerator;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.version.common.version.Versioned;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class Main extends JavaPlugin {

    private static final String FILE_LOCATION = System.getProperty("net.okocraft.box.datagenerator.output.dir");
    private static final String PREVIOUS_VERSION = System.getProperty("net.okocraft.box.datagenerator.previous-version");

    @Override
    public void onEnable() {
        this.generateData();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.isOp()) {
            this.generateData();
        }
        return true;
    }

    private void generateData() {
        var dir = FILE_LOCATION != null ? Path.of(FILE_LOCATION) : this.getDataFolder().toPath().resolve("generated");

        if (Files.isDirectory(dir)) {
            try (var walk = Files.walk(dir)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(filepath -> {
                        try {
                            Files.deleteIfExists(filepath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            } catch (IOException | RuntimeException e) {
                this.getSLF4JLogger().error("Failed to delete old output dir", e);
                return;
            }
        }

        var generator = Versioned.implementations(this.getClassLoader())
            .stream()
            .filter(impl -> impl.version().isSame(MCDataVersion.current()))
            .map(DataGenerator::new)
            .findFirst().orElse(null);

        if (generator == null) {
            this.getSLF4JLogger().error("No version impl found.");
            return;
        }

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            this.getSLF4JLogger().error("Failed to create directories", e);
            return;
        }

        this.getSLF4JLogger().info("Generating data and saving to {}", dir.toAbsolutePath());

        try {
            generator.defaultItems(dir);

            if (PREVIOUS_VERSION != null) {
                generator.newDefaultItems(dir, PREVIOUS_VERSION);
            }

            generator.uncategorizedItems(dir);
        } catch (IOException | UncheckedIOException e) {
            this.getSLF4JLogger().error("Failed to generate data", e);
        }
    }
}
