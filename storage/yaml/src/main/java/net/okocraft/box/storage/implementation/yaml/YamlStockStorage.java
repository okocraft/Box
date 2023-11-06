package net.okocraft.box.storage.implementation.yaml;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.storage.api.holder.LoggerHolder;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

class YamlStockStorage implements StockStorage {

    private final Path stockDirectory;

    YamlStockStorage(@NotNull Path rootDirectory) {
        this.stockDirectory = rootDirectory.resolve("stock");
    }

    @Override
    public void init() throws Exception {
        if (!Files.isDirectory(this.stockDirectory)) {
            Files.createDirectories(this.stockDirectory);
        }
    }

    @Override
    public @NotNull Collection<StockData> loadStockData(@NotNull UUID uuid) throws Exception {
        var filePath = this.stockDirectory.resolve(uuid + ".yml");

        if (!Files.isRegularFile(filePath)) {
            return Collections.emptyList();
        }

        var loadedData = new ArrayList<StockData>(50);

        try (var reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            reader.lines()
                    .map(line -> readLine(uuid, line))
                    .filter(Objects::nonNull)
                    .forEach(loadedData::add);
        }

        return loadedData;
    }

    @Override
    public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData, @NotNull Int2IntFunction itemIdRemapper) throws Exception {
        var file = this.stockDirectory.resolve(uuid + ".yml");

        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, YamlFileOptions.WRITE)) {
            for (var stock : stockData) {
                if (0 != stock.amount()) {
                    writer.write('\'');
                    writer.write(itemIdRemapper.applyAsInt(stock.itemId()));
                    writer.write("': ");
                    writer.write(stock.amount());
                    writer.newLine();
                }
            }
        }
    }

    private static @Nullable StockData readLine(@NotNull UUID uuid, @NotNull String line) {
        if (line.isEmpty() || line.equals("{}")) {
            return null;
        }

        int itemId;

        try {
            int start = line.indexOf('\'') + 1;
            int end = start == 1 ? line.indexOf('\'', start) : line.indexOf(":");
            itemId = Integer.parseInt(line.substring(start, end));
        } catch (Exception e) {
            LoggerHolder.get().warning("Could not parse stock data: " + line + " (" + uuid + ")");
            return null;
        }

        int amount;

        try {
            amount = Integer.parseInt(line.substring(line.lastIndexOf(":") + 2));
        } catch (Exception e) {
            LoggerHolder.get().warning("Could not parse stock data: " + line + " (" + uuid + ")");
            return null;
        }

        return 0 < amount ? new StockData(itemId, amount) : null;
    }
}
