package net.okocraft.box.plugin.database;

import net.okocraft.box.plugin.database.connector.Database;
import net.okocraft.box.plugin.database.table.ItemTable;
import net.okocraft.box.plugin.database.table.MasterTable;
import net.okocraft.box.plugin.database.table.PlayerTable;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.model.item.Stock;
import net.okocraft.box.util.UnsafeRunnable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
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

    public Storage(@NotNull Database.Type type) { // TODO
        database = Database.connectSQLite(Path.of("./plugins/Box/data.db"));
        database.start();

        playerTable = new PlayerTable(database, "box_");
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
    public CompletableFuture<User> loadUser(@NotNull UUID uuid, @NotNull String name) {
        return makeFuture(() -> {
            User user = playerTable.updateUser(uuid, name);
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
    public CompletableFuture<Stock> createStock(@NotNull User user, @NotNull Item item) {
        return makeFuture(() -> masterTable.createOrLoadStock(user, item));
    }

    @NotNull
    public CompletableFuture<Void> saveDefaultUserData(@NotNull User user) {
        return makeFuture(() -> masterTable.saveDefaultUserData(user));
    }

    @NotNull
    public CompletableFuture<Item> registerItem(@NotNull ItemStack item) {
        return makeFuture(() -> itemTable.registerItem(item));
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
