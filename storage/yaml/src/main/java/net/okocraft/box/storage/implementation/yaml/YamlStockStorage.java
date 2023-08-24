package net.okocraft.box.storage.implementation.yaml;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.stock.UserStockHolderFactory;
import net.okocraft.box.storage.api.holder.LoggerHolder;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

class YamlStockStorage implements StockStorage {

    private final Path stockDirectory;

    YamlStockStorage(@NotNull Path rootDirectory) {
        this.stockDirectory = rootDirectory.resolve("stock");
    }

    @Override
    public void init() throws Exception {
        if (Files.exists(stockDirectory)) {
            return;
        }

        var parent = stockDirectory.getParent();

        if (parent != null) {
            var oldStockDirectory = parent.resolve("stocks");

            if (Files.exists(oldStockDirectory)) {
                try {
                    Files.move(oldStockDirectory, stockDirectory, StandardCopyOption.REPLACE_EXISTING);
                    return;
                } catch (IOException ignored) {
                }
            }
        }

        Files.createDirectories(stockDirectory);
    }

    @Override
    public @NotNull UserStockHolder loadUserStockHolder(@NotNull BoxUser user) throws Exception {
        var filePath = stockDirectory.resolve(user.getUUID() + ".yml");

        if (!Files.exists(filePath)) {
            return UserStockHolderFactory.create(user);
        }

        var loadedData = new ArrayList<StockData>(50);

        try (var reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            reader.lines()
                    .map(line -> readLine(user.getUUID(), line))
                    .filter(Objects::nonNull)
                    .forEach(loadedData::add);
        }

        return UserStockHolderFactory.create(user, loadedData);
    }

    @Override
    public void saveUserStockHolder(@NotNull UserStockHolder stockHolder) throws Exception {
        var file = stockDirectory.resolve(stockHolder.getUser().getUUID() + ".yml");

        var builder = new StringBuilder(20);

        try (var writer =
                     Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                             StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (var stock : stockHolder.toStockDataCollection()) {
                if (0 != stock.amount()) {
                    builder.append('\'')
                            .append(stock.itemInternalId())
                            .append('\'')
                            .append(':')
                            .append(' ')
                            .append(stock.amount())
                            .append(System.lineSeparator());
                    writer.write(builder.toString());
                    builder.setLength(0);
                }
            }
        }
    }

    private @Nullable StockData readLine(@NotNull UUID uuid, @NotNull String line) {
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

        return new StockData(itemId, amount);
    }
}
