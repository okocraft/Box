package net.okocraft.box.database;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerData {

    private final Database database;
    private final ItemTable itemTable;
    private final PlayerDataTable playerDataTable;

    private final Map<Player, Map<ItemStack, Boolean>> autostore = new HashMap<>();
    private final Map<Player, Map<ItemStack, Integer>> stock = new HashMap<>();
    private final Set<String> players = new HashSet<>();

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public PlayerData(Path dbPath) {
        try {
            this.database = new Database(dbPath);
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }

        this.itemTable = new ItemTable(database);
        this.playerDataTable = new PlayerDataTable(database, itemTable);

        loadPlayerNames();
    }

    ItemTable getItemTable() {
        return itemTable;
    }

    private void loadPlayerNames() {
        database.query("SELECT player FROM box_playerdata", rs -> {
            try {
                while (rs.next()) {
                    String playerName = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("player"))).getName();
                    if (playerName != null) {
                        players.add(playerName);
                    }
                }
            } catch (IllegalArgumentException | SQLException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    public List<String> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * @see Database#dispose()
     */
    public void dispose() {
        database.dispose();
    }

    public void loadCache(Player player) {
        stock.put(player, getStockAll(player));
        autostore.put(player, getAutoStoreAll(player));
    }

    public void removeCache(Player player) {
        playerDataTable.setAutoStoreAll(player, autostore.remove(player));
        playerDataTable.setStockAll(player, stock.remove(player));
    }

    public void setAutoStoreAll(OfflinePlayer player, boolean enabled) {
        if (player.isOnline()) {
            getAutoStoreAll(player).replaceAll((item, value) -> enabled);
        }

        threadPool.submit(() -> playerDataTable.setAutoStoreAll(player, enabled));
    }

    public void setAutoStoreAll(OfflinePlayer player, Map<ItemStack, Boolean> enabled) {
        Map<ItemStack, Boolean> replaced = new HashMap<>();
        enabled.forEach((item, value) -> {
            if (item != null) {
                ItemStack clone = item.clone();
                clone.setAmount(1);
                replaced.put(clone, value);
            }
        });
        if (player.isOnline()) {
            getAutoStoreAll(player).replaceAll((item, value) -> replaced.get(item));
        }

        threadPool.submit(() -> playerDataTable.setAutoStoreAll(player, replaced));
    }

    public void setAutoStore(OfflinePlayer player, ItemStack item, boolean enabled) {
        if (item == null) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getAutoStoreAll(player).put(clone, enabled);
        }

        threadPool.submit(() -> playerDataTable.setAutoStore(player, clone, enabled));
    }

    public boolean getAutoStore(OfflinePlayer player, ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        return player.isOnline()
                ? getAutoStoreAll(player).getOrDefault(clone, false)
                : playerDataTable.getAutoStore(player, clone); 
    }

    public Map<ItemStack, Boolean> getAutoStoreAll(OfflinePlayer player) {
        if (player.isOnline()) {
            if (autostore.containsKey(player)) {
                return autostore.get(player);
            } else {
                Map<ItemStack, Boolean> result = playerDataTable.getAutoStoreAll(player);
                autostore.put(player.getPlayer(), result);
                return result;
            }
        }

        return playerDataTable.getAutoStoreAll(player);
    }

    public void setStock(OfflinePlayer player, ItemStack item, int stock) {
        if (item == null) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getStockAll(player).put(clone, stock);
        }

        threadPool.submit(() -> playerDataTable.setStock(player, clone, stock));
    }

    public void setStockAll(OfflinePlayer player, Map<ItemStack, Integer> stock) {
        Map<ItemStack, Integer> clone = new HashMap<>();
        stock.forEach((item, value) -> {
            if (item != null) {
                ItemStack cloneItem = item.clone();
                cloneItem.setAmount(1);
                clone.put(cloneItem, value);
            }
        });
        if (player.isOnline()) {
            getStockAll(player).replaceAll((item, value) -> clone.getOrDefault(item, 0));
        }

        threadPool.submit(() -> playerDataTable.setStockAll(player, clone));
    }

    public void addStock(OfflinePlayer player, ItemStack item, int amount) {
        if (item == null) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getStockAll(player).put(clone, getStock(player, clone) + amount);
        }

        threadPool.submit(() -> playerDataTable.addStock(player, clone, amount));
    }

    public int getStock(OfflinePlayer player, ItemStack item) {
        if (item == null) {
            return 0;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        return player.isOnline()
                ? getStockAll(player).getOrDefault(clone, 0)
                : playerDataTable.getStock(player, clone); 
    }

    public Map<ItemStack, Integer> getStockAll(OfflinePlayer player) {
        if (player.isOnline()) {
            if (stock.containsKey(player)) {
                return stock.get(player);
            } else {
                Map<ItemStack, Integer> result = playerDataTable.getStockAll(player);
                stock.put(player.getPlayer(), result);
                return result;
            }
        }

        return playerDataTable.getStockAll(player);
    }

    public boolean storeAll(Player player) {
        boolean isModified = false;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (itemTable.getId(item) == -1) {
                continue;
            }
            int stock = getStock(player, item);
            int amount = item.getAmount();
            amount -= player.getInventory().removeItem(item).values().stream().map(ItemStack::getAmount).mapToInt(Integer::valueOf).sum();
            setStock(player, item, stock + amount);
            isModified = true;
        }

        return isModified;
    }
}