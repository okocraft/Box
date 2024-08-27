package net.okocraft.box.datagenerator;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.feature.category.internal.category.defaults.DefaultCategories;
import net.okocraft.box.feature.category.internal.category.defaults.DefaultCategory;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.storage.api.util.SneakyThrow;
import net.okocraft.box.version.common.version.Versioned;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class DataGenerator {

    private final List<String> defaultItems;
    private final Map<String, String> renamedItems;

    DataGenerator(@NotNull Versioned impl) {
        this.defaultItems = impl.defaultItems().map(DefaultItem::plainName).distinct().sorted().toList();
        this.renamedItems = impl.loadRenamedItems();
    }

    public void defaultItems(@NotNull Path dir) throws IOException {
        try (var writer = Files.newBufferedWriter(dir.resolve(Bukkit.getMinecraftVersion() + ".txt"))) {
            this.defaultItems.forEach(name -> {
                try {
                    writer.write(name);
                    writer.newLine();
                } catch (IOException e) {
                    SneakyThrow.sneaky(e);
                }
            });
        }
    }

    public void newDefaultItems(@NotNull Path dir, @NotNull String prev) throws IOException {
        var items = new ObjectOpenHashSet<String>();

        try (var in = this.getClass().getClassLoader().getResourceAsStream("generated/items/" + prev + ".txt")) {
            if (in == null) {
                throw new IOException(prev + ".txt was not found in /generated/items");
            }

            try (var reader = new BufferedReader(new InputStreamReader(in));
                 var lines = reader.lines()) {
                lines.map(String::trim).forEach(items::add);
            }
        }

        for (var entry : this.renamedItems.entrySet()) {
            if (items.remove(entry.getKey())) {
                items.add(entry.getValue());
            }
        }

        try (var writer = Files.newBufferedWriter(dir.resolve(Bukkit.getMinecraftVersion() + "-new-items.txt"))) {
            this.defaultItems.stream()
                .filter(Predicate.not(items::contains))
                .forEach(name -> {
                    try {
                        writer.write(name);
                        writer.newLine();
                    } catch (IOException e) {
                        SneakyThrow.sneaky(e);
                    }
                });
        }
    }

    @SuppressWarnings("deprecation")
    public void uncategorizedItems(@NotNull Path dir) throws IOException {
        var categorizedItems =
            DefaultCategories.loadDefaultCategories(MCDataVersion.current())
                .stream()
                .map(DefaultCategory::itemNames)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        try (var writer = Files.newBufferedWriter(dir.resolve(Bukkit.getMinecraftVersion() + "-uncategorized-items.txt"))) {
            this.defaultItems.stream()
                .filter(Predicate.not(categorizedItems::contains))
                .forEach(name -> {
                    try {
                        writer.write("  - ");
                        writer.write(String.valueOf(Bukkit.getUnsafe().getDataVersion()));
                        writer.write(":");
                        writer.write(name);
                        writer.newLine();
                    } catch (IOException e) {
                        SneakyThrow.sneaky(e);
                    }
                });
        }
    }
}
