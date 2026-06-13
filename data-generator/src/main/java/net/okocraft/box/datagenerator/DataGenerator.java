package net.okocraft.box.datagenerator;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.feature.category.internal.category.defaults.DefaultCategories;
import net.okocraft.box.feature.category.internal.category.defaults.DefaultCategory;
import net.okocraft.box.item.DefaultItem;
import net.okocraft.box.item.DefaultItemProvider;
import net.okocraft.box.item.MinecraftVersioning;
import net.okocraft.box.item.RenamedItems;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DataGenerator {

    private final MCDataVersion version;
    private final List<String> defaultItems;
    private final Map<String, String> renamedItems;

    DataGenerator() {
        DefaultItemProvider provider = MinecraftVersioning.createDefaultItemProvider();
        this.version = provider.version();
        this.defaultItems = provider.provide().map(DefaultItem::plainName).distinct().sorted().toList();
        this.renamedItems = RenamedItems.loadVersionFromResource(this.version);
    }

    public @NotNull MCDataVersion version() {
        return this.version;
    }

    public void defaultItems(@NotNull Path dir) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve(Bukkit.getMinecraftVersion() + ".txt"))) {
            for (String name : this.defaultItems) {
                writer.write(name);
                writer.newLine();
            }
        }
    }

    public void newDefaultItems(@NotNull Path dir, @NotNull String prev) throws IOException {
        ObjectOpenHashSet<String> items = new ObjectOpenHashSet<>();

        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("generated/items/" + prev + ".txt")) {
            if (in == null) {
                throw new IOException(prev + ".txt was not found in /generated/items");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                 Stream<String> lines = reader.lines()) {
                lines.map(String::trim).forEach(items::add);
            }
        }

        for (Map.Entry<String, String> entry : this.renamedItems.entrySet()) {
            if (items.remove(entry.getKey())) {
                items.add(entry.getValue());
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve(Bukkit.getMinecraftVersion() + "-new-items.txt"))) {
            for (String name : this.defaultItems) {
                if (items.contains(name)) {
                    continue;
                }

                writer.write(name);
                writer.newLine();
            }
        }
    }

    public void uncategorizedItems(@NotNull Path dir) throws IOException {
        Set<String> categorizedItems =
            DefaultCategories.loadDefaultCategories(MCDataVersion.current())
                .stream()
                .map(DefaultCategory::itemNames)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve(Bukkit.getMinecraftVersion() + "-uncategorized-items.txt"))) {
            for (String name : this.defaultItems) {
                if (categorizedItems.contains(name)) {
                    continue;
                }

                writer.write("  - ");
                writer.write(String.valueOf(MCDataVersion.current().dataVersion()));
                writer.write(":");
                writer.write(name);
                writer.newLine();
            }
        }
    }
}
