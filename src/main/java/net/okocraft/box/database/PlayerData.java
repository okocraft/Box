package net.okocraft.box.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;
import net.okocraft.box.config.Config;

/**
 * 
 * プレイヤーのデータを設定・取得できる。
 * このクラスはキャッシュを扱い、なおかつキャッシュへ変更が加えられたらデータベースに非同期でデータ保存SQLを発行する。
 * キャッシュが削除された瞬間も一応データベースにキャッシュの内容を保存する。
 */
public class PlayerData {

    private final Database database;
    private final ItemTable itemTable;
    private final PlayerTable playerTable;
    private final MasterTable masterTable;

    private final Map<Player, Map<ItemStack, Boolean>> autostore = new HashMap<>();
    private final Map<Player, Map<ItemStack, Integer>> stock = new HashMap<>();

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    /**
     * コンストラクタ。
     * 
     * @param config 設定ファイル。データベース設定を取得するために必要とされる
     * 
     * @deprecated 内部利用限定。このコンストラクタを使わず、{@code Box.getInstance().getAPI().getPlayerData()}を使用すること。
     */
    @Deprecated
    public PlayerData(Config config) {
        Box plugin = Box.getInstance();

        Database usingDatabase = null;
        if (config.usingMySQL()) {
            try {
                usingDatabase = new Database(
                    config.getMySQLHost(),
                    config.getMySQLPort(),
                    config.getMySQLUser(),
                    config.getMySQLPass(),
                    config.getMySQLDatabaseName()
                );
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Cannot connect to MySQL server", e);
                plugin.getLogger().warning("Switching to SQLite");
            }
        }

        if (usingDatabase == null) { 
            try {
                usingDatabase = new Database(plugin.getDataFolder().toPath().resolve("database.db"));
            } catch (SQLException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        this.database = usingDatabase;
        this.itemTable = new ItemTable(database);
        this.playerTable = new PlayerTable(database);
        this.masterTable = new MasterTable(database, itemTable, playerTable);
    }

    ItemTable getItemTable() {
        return itemTable;
    }

    PlayerTable getPlayerTable() {
        return playerTable;
    }

    /**
     * データベースに登録されている全てのプレイヤーの名前を返す。
     * 
     * @return 名前のリスト
     */
    public List<String> getPlayers() {
        return getPlayerTable().getAllName();
    }

    /**
     * データベースの接続を切断する。
     */
    public void dispose() {
        Bukkit.getOnlinePlayers().forEach(this::removeCache);
        threadPool.submit(() -> database.dispose());
        try {
            threadPool.awaitTermination(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * プレイヤーのストックやautostoreなどのデータをキャッシュに入れておく。
     * 
     * @param player キャッシュを取るプレイヤー
     */
    public void loadCache(Player player) {
        stock.put(player, getStockAll(player));
        autostore.put(player, getAutoStoreAll(player));
        playerTable.loadCache(player);
    }

    /**
     * キャッシュを削除し、そのデータをデータベースに保存する。
     * キャッシュが取られていないプレイヤーだった場合の動きは定義されていないが、エラーが起こることはない。
     *
     * @param player キャッシュを削除するプレイヤー。
     */
    public void removeCache(Player player) {
        masterTable.setAutoStoreAll(player, autostore.remove(player));
        masterTable.setStockAll(player, stock.remove(player));
        playerTable.removeCache(player);
    }

    /**
     * プレイヤーのautostoreの全てを{@code enabled}に設定する。
     * 
     * @param player autostoreを設定するプレイヤー
     * @param enabled autostoreの値
     */
    public void setAutoStoreAll(OfflinePlayer player, boolean enabled) {
        if (player.isOnline()) {
            getAutoStoreAll(player).replaceAll((item, value) -> enabled);
        }

        threadPool.submit(() -> masterTable.setAutoStoreAll(player, enabled));
    }

    /**
     * プレイヤーのautostoreを指定したマップの通りに設定する。マップにあるアイテムがデータベースに存在しない場合は無視される。
     * 
     * @param player autostoreを設定するプレイヤー
     * @param enabled 各アイテムについてのautostoreの値
     */
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

        threadPool.submit(() -> masterTable.setAutoStoreAll(player, replaced));
    }

    /**
     * プレイヤーのautostoreを指定した通りに設定する。アイテムがデータベースに存在しない場合は無視される。
     * 
     * @param player autostoreを設定するプレイヤー
     * @param item autostoreを設定するアイテム
     * @param enabled autostoreの値
     */
    public void setAutoStore(OfflinePlayer player, ItemStack item, boolean enabled) {
        if (item == null || itemTable.getId(item) == -1) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getAutoStoreAll(player).put(clone, enabled);
        }

        threadPool.submit(() -> masterTable.setAutoStore(player, clone, enabled));
    }

    /**
     * autostore値を取得する。
     * 
     * @param player 値を取得するプレイヤー
     * @param item 値を取得するアイテム
     * @return 値
     */
    public boolean getAutoStore(OfflinePlayer player, ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        return player.isOnline()
                ? getAutoStoreAll(player).getOrDefault(clone, false)
                : masterTable.getAutoStore(player, clone); 
    }

    /**
     * すべてのアイテムについて、autostore値を取得する。
     * 
     * @param player 値を取得するプレイヤー
     * @return アイテムとautostore値のマップ
     */
    public Map<ItemStack, Boolean> getAutoStoreAll(OfflinePlayer player) {
        if (player.isOnline()) {
            Map<ItemStack, Boolean> result;
            if (autostore.containsKey(player)) {
                result = autostore.get(player);
            } else {
                result = masterTable.getAutoStoreAll(player);
                autostore.put(player.getPlayer(), result);
            }
            result.keySet().removeIf(item -> Objects.isNull(item) || itemTable.getId(item) == -1);
            result.values().removeIf(Objects::isNull);
            return result;
        }

        return masterTable.getAutoStoreAll(player);
    }

    /**
     * プレイヤーのアイテムのストックを設定する。
     * 
     * @param player アイテムのストックを設定するプレイヤー
     * @param item アイテム
     * @param stock ストック
     */
    public void setStock(OfflinePlayer player, ItemStack item, int stock) {
        if (item == null || itemTable.getId(item) == -1) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getStockAll(player).put(clone, stock);
        }

        threadPool.submit(() -> masterTable.setStock(player, clone, stock));
    }

    /**
     * プレイヤーのアイテムのストックをマップの通りに設定する。
     * 
     * @param player プレイヤー
     * @param stock アイテムとストックのマップ
     */
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

        threadPool.submit(() -> masterTable.setStockAll(player, clone));
    }

    /**
     * アイテムのストックを指定した数だけ合算する。
     * 
     * @param player ストックを合算するプレイヤー
     * @param item ストックを合算するアイテム
     * @param amount 合算される値
     */
    public void addStock(OfflinePlayer player, ItemStack item, int amount) {
        if (item == null) {
            return;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        if (player.isOnline()) {
            getStockAll(player).put(clone, getStock(player, clone) + amount);
        }

        threadPool.submit(() -> masterTable.addStock(player, clone, amount));
    }

    /**
     * アイテムのストックを取得する。
     * 
     * @param player ストックを取得するプレイヤー
     * @param item ストックを取得するアイテム
     * @return ストック値
     */
    public int getStock(OfflinePlayer player, ItemStack item) {
        if (item == null) {
            return 0;
        }
        ItemStack clone = item.clone();
        clone.setAmount(1);
        return player.isOnline()
                ? getStockAll(player).getOrDefault(clone, 0)
                : masterTable.getStock(player, clone); 
    }

    /**
     * すべてのアイテムのストックを取得する。
     * 
     * @param player ストックを取得するプレイヤー
     * @return アイテムとストックのマップ
     */
    public Map<ItemStack, Integer> getStockAll(OfflinePlayer player) {
        if (player.isOnline()) {
            Map<ItemStack, Integer> result;
            if (stock.containsKey(player)) {
                result = stock.get(player);
            } else {
                result = masterTable.getStockAll(player);
                stock.put(player.getPlayer(), result);
            }
            result.keySet().removeIf(item -> Objects.isNull(item) || itemTable.getId(item) == -1);
            result.values().removeIf(Objects::isNull);
            return result;
        }

        return masterTable.getStockAll(player);
    }

    /**
     * プレイヤーのインベントリのアイテムをすべて預ける。
     * 
     * @param player プレイヤー
     * @return 預けた結果、プレイヤーのインベントリが変更されたらtrue、さもなくばfalse
     */
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