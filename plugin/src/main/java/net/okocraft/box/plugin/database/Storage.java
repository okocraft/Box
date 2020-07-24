package net.okocraft.box.plugin.database;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.config.GeneralConfig;
import net.okocraft.box.plugin.database.connector.Database;
import net.okocraft.box.plugin.database.table.ItemTable;
import net.okocraft.box.plugin.database.table.MasterTable;
import net.okocraft.box.plugin.database.table.PlayerTable;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.model.item.Stock;
import net.okocraft.box.plugin.result.UserCheckResult;
import net.okocraft.box.plugin.util.UnsafeRunnable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Storage {

    private final Database database;
    private final PlayerTable playerTable;
    private final ItemTable itemTable;
    private final MasterTable masterTable;

    private final ExecutorService executor;

    private final Set<Item> items;

    public Storage(@NotNull Box plugin) {
        this(plugin, plugin.getGeneralConfig().getDatabaseType());
    }

    public Storage(@NotNull Box plugin, @NotNull Database.Type type) {
        if (type == Database.Type.MYSQL) {
            GeneralConfig config = plugin.getGeneralConfig();

            database = Database.connectMySQL(
                    config.getDatabaseAddress() + ":" + config.getDatabasePort(),
                    config.getDatabaseName(),
                    config.getDatabaseUserName(),
                    config.getDatabasePassword(),
                    config.isUsingSSL()
            );
        } else {
            database = Database.connectSQLite(plugin.getDataFolder().toPath().resolve("data.db"));
        }

        database.start();

        playerTable = new PlayerTable(plugin, database, "box_");
        itemTable = new ItemTable(database, "box_");

        try {
            items = itemTable.loadAllItems();
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }

        masterTable = new MasterTable(database, this, "box_");

        executor = Executors.newSingleThreadExecutor();

        UnsafeRunnable task = () -> itemTable.updateItems(items);
        executor.submit(task.toRunnable());
    }

    public void shutdown() {
        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        database.shutdown();
    }

    public Optional<Item> getItemById(int id) {
        return items.stream().filter(i -> i.getInternalID() == id).findFirst();
    }

    @NotNull
    public Set<Item> getItems() {
        return items;
    }

    @NotNull
    public CompletableFuture<UserCheckResult> checkUser(@NotNull UUID uuid, @NotNull String name) {
        return makeFuture(() -> playerTable.checkUser(uuid, name));
    }

    @NotNull
    public CompletableFuture<User> loadUser(@NotNull UUID uuid) {
        return makeFuture(() -> {
            User user = playerTable.loadUser(uuid);
            return masterTable.loadUserData(user);
        });
    }

    @NotNull
    public CompletableFuture<Void> saveUser(@NotNull User user) {
        return makeFuture(() -> masterTable.saveUserData(user));
    }

    @NotNull
    public CompletableFuture<Optional<User>> searchUser(@NotNull String name) {
        return makeFuture(() -> {
            Optional<User> user = playerTable.searchUser(name);
            if (user.isPresent()) {
                return Optional.of(masterTable.loadUserData(user.get()));
            } else {
                return Optional.empty();
            }
        });
    }

    @NotNull
    public CompletableFuture<Stock> createStock(int playerId, @NotNull Item item) {
        return makeFuture(() -> masterTable.createOrLoadStock(playerId, item));
    }

    @NotNull
    public CompletableFuture<Void> saveDefaultUserData(@NotNull UUID uuid) {
        return makeFuture(() -> {
            User user = playerTable.loadUser(uuid);
            masterTable.saveDefaultUserData(user);
        });
    }

    @NotNull
    public CompletableFuture<Item> registerItem(@NotNull ItemStack item) {
        return makeFuture(() -> {
            Item boxItem = itemTable.registerItem(item);
            items.add(boxItem);
            return boxItem;
        });
    }

    @NotNull
    public CompletableFuture<Void> saveCustomName(@NotNull Item item) {
        return makeFuture(() -> itemTable.saveCustomName(item));
    }

    @NotNull
    private CompletableFuture<Void> makeFuture(@NotNull UnsafeRunnable task) {
        return CompletableFuture.runAsync(task.toRunnable(), executor);
    }

    @NotNull
    private <T> CompletableFuture<T> makeFuture(@NotNull Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new CompletionException(e);
                }
            }
        }, executor);
    }
}
