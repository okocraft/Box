package net.okocraft.box.core.storage.implementations.yaml;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.core.model.stock.UserStockHolderImpl;
import net.okocraft.box.core.storage.model.stock.StockStorage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

class YamlStockStorage implements StockStorage {

    private final Path stockDirectory;

    YamlStockStorage(@NotNull Path rootDirectory) {
        this.stockDirectory = rootDirectory.resolve("stocks");
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(stockDirectory);
    }

    @Override
    public void close() {
    }

    @Override
    public @NotNull UserStockHolder loadUserStockHolder(@NotNull BoxUser user) throws Exception {
        var filePath = stockDirectory.resolve(user.getUUID() + ".yml");

        if (!Files.exists(filePath)) {
            return new UserStockHolderImpl(user);
        }

        var file = YamlConfiguration.create(filePath);

        file.load();

        var loadedData = new ArrayList<StockData>();

        for (var key : file.getKeyList()) {
            int id;

            try {
                id = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                BoxProvider.get().getLogger().warning("Could not parse key to id: " + key + " (" + user.getUUID() + ")");
                continue;
            }

            var item = BoxProvider.get().getItemManager().getBoxItem(id);

            if (item.isEmpty()) {
                BoxProvider.get().getLogger().warning("Unknown id: " + id + " (" + user.getUUID() + ")");
                continue;
            }

            loadedData.add(new StockData(item.get(), file.getInteger(key)));
        }

        return new UserStockHolderImpl(user, loadedData);
    }

    @Override
    public void saveUserStockHolder(@NotNull UserStockHolder stockHolder) throws Exception {
        var file = YamlConfiguration.create(stockDirectory.resolve(stockHolder.getUser().getUUID() + ".yml"));

        for (var stock : stockHolder.toStockDataCollection()) {
            var key = String.valueOf(stock.item().getInternalId());
            int amount = stock.amount();

            if (0 < amount) {
                file.set(key, amount);
            } else {
                file.set(key, null);
            }
        }

        file.save();
    }
}
