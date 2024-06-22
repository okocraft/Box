package net.okocraft.box.storage.implementation.yaml;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.util.SneakyThrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

class YamlStockStorage implements StockStorage {

    private final Path stockDirectory;

    YamlStockStorage(@NotNull Path rootDirectory) {
        this.stockDirectory = rootDirectory.resolve("stock");
    }

    public void init() throws IOException {
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

        return loadFromFile(filePath, uuid.toString());
    }

    @Override
    public void saveStockData(@NotNull UUID uuid, @NotNull Collection<StockData> stockData) throws Exception {
        var file = this.stockDirectory.resolve(uuid + ".yml");

        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, YamlFileOptions.WRITE)) {
            for (var stock : stockData) {
                if (0 != stock.amount()) {
                    writer.write('\'');
                    writer.write(Integer.toString(stock.itemId()));
                    writer.write("': ");
                    writer.write(Integer.toString(stock.amount()));
                    writer.newLine();
                }
            }
        }
    }

    @Override
    public void remapItemIds(@NotNull Int2IntMap remappedIdMap) throws Exception {
        try (var list = Files.list(this.stockDirectory)) {
            list.forEach(filepath -> {
                if (!Files.isRegularFile(filepath)) {
                    return;
                }

                try {
                    this.remapItemIds(remappedIdMap, filepath);
                } catch (IOException e) {
                    SneakyThrow.sneaky(e);
                }
            });
        }
    }

    private void remapItemIds(@NotNull Int2IntMap remappedIdMap, @NotNull Path filepath) throws IOException {
        var stockData = loadFromFile(filepath, filepath.getFileName().toString());
        var idToAmountMap = new Int2IntOpenHashMap();

        for (var stock : stockData) {
            idToAmountMap.put(stock.itemId(), stock.amount());
        }

        for (var remapEntry : remappedIdMap.int2IntEntrySet()) {
            int oldId = remapEntry.getIntKey();
            int newId = remapEntry.getIntValue();
            int amount = idToAmountMap.remove(oldId);

            idToAmountMap.merge(newId, amount, Integer::sum);
        }

        try (var writer = Files.newBufferedWriter(filepath, StandardCharsets.UTF_8, YamlFileOptions.WRITE)) {
            for (var entry : idToAmountMap.int2IntEntrySet()) {
                writer.write('\'');
                writer.write(Integer.toString(entry.getIntKey()));
                writer.write("': ");
                writer.write(Integer.toString(entry.getIntValue()));
                writer.newLine();
            }
        }
    }

    private static @NotNull List<StockData> loadFromFile(Path filePath, String identity) throws IOException {
        var loadedData = new ArrayList<StockData>(50);

        try (var reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            reader.lines()
                    .map(line -> readLine(identity, line))
                    .filter(Objects::nonNull)
                    .forEach(loadedData::add);
        }

        return loadedData;
    }

    private static @Nullable StockData readLine(@NotNull String source, @NotNull String line) {
        if (line.isEmpty() || line.equals("{}")) {
            return null;
        }

        int itemId;
        int amount;

        try {
            int start = line.indexOf('\'') + 1;
            int end = start == 1 ? line.indexOf('\'', start) : line.indexOf(":");
            itemId = Integer.parseInt(line.substring(start, end));
            amount = Integer.parseInt(line.substring(line.lastIndexOf(":") + 2));
        } catch (Exception e) {
            BoxLogger.logger().warn("Could not parse stock data: {} ({})", line, source);
            return null;
        }

        return 0 < amount ? new StockData(itemId, amount) : null;
    }
}
