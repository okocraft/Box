package net.okocraft.box.core.model.queue;

import net.okocraft.box.core.model.loader.UserStockHolderLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AutoSaveQueue {

    private final List<UserStockHolderLoader> queue = new ArrayList<>();

    public void enqueue(@NotNull UserStockHolderLoader loader) {
        if (!queue.contains(loader)) {
            queue.add(loader);
        }
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
