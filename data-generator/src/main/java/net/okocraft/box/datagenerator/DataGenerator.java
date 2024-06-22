package net.okocraft.box.datagenerator;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
import java.util.function.Predicate;

class DataGenerator {

    private final Versioned impl;

    DataGenerator(@NotNull Versioned impl) {
        this.impl = impl;
    }

    public void defaultItems(@NotNull Path dir) throws IOException {
        try (var writer = Files.newBufferedWriter(dir.resolve(Bukkit.getMinecraftVersion() + ".txt"))) {
            this.impl.defaultItems()
                    .map(DefaultItem::plainName)
                    .sorted()
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

        for (var entry : this.impl.loadRenamedItems().entrySet()) {
            if (items.remove(entry.getKey())) {
                items.add(entry.getValue());
            }
        }

        try (var writer = Files.newBufferedWriter(dir.resolve(Bukkit.getMinecraftVersion() + "-new-items.txt"))) {
            this.impl.defaultItems()
                    .map(DefaultItem::plainName)
                    .sorted()
                    .filter(Predicate.not(items::contains))
                    .forEach(name -> {
                        try {
                            writer.write(String.valueOf(Bukkit.getUnsafe().getDataVersion()));
                            writer.write(": ");
                            writer.write(name);
                            writer.newLine();
                        } catch (IOException e) {
                            SneakyThrow.sneaky(e);
                        }
                    });
        }
    }
}
