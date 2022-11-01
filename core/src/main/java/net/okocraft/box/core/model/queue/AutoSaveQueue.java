package net.okocraft.box.core.model.queue;

import net.okocraft.box.core.model.loader.UserStockHolderLoader;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoSaveQueue {

    private final Set<UserStockHolderLoader> queue = new HashSet<>();

    public void enqueue(@NotNull UserStockHolderLoader loader) {
        queue.add(loader);
    }

    public void dequeue(@NotNull UserStockHolderLoader loader) {
        queue.remove(loader);
    }

    public List<UserStockHolderLoader> getQueue() {
        return List.copyOf(queue);
    }

    public void clear() {
        queue.clear();
    }
}
