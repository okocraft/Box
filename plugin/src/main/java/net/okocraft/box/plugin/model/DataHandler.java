package net.okocraft.box.plugin.model;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.item.Item;
import org.jetbrains.annotations.NotNull;

public final class DataHandler {

    private final Box plugin;

    public DataHandler(@NotNull Box plugin) {
        this.plugin = plugin;
    }

    public void setAmount(@NotNull User user, @NotNull Item item, int amount) {
        user.setAmount(item, amount);

        user.getStock(item).ifPresent(s -> plugin.getStorage().saveStock(user, s));
    }

    public int increase(@NotNull User user, @NotNull Item item) {
        return increase(user, item, 1);
    }

    public int increase(@NotNull User user, @NotNull Item item, int increment) {
        int amount = user.increase(item, increment);

        user.getStock(item).ifPresent(s -> plugin.getStorage().saveStock(user, s));

        return amount;
    }

    public int decrease(@NotNull User user, @NotNull Item item) {
        return decrease(user, item, 1);
    }

    public int decrease(@NotNull User user, @NotNull Item item, int decrement) {
        int amount = user.decrease(item, decrement);

        user.getStock(item).ifPresent(s -> plugin.getStorage().saveStock(user, s));

        return amount;
    }

    public void setAutoStore(@NotNull User user, @NotNull Item item, boolean enable) {
        user.setAutoStore(item, enable);

        user.getStock(item).ifPresent(s -> plugin.getStorage().saveStock(user, s));
    }

    public boolean toggleAutoStore(@NotNull User user, @NotNull Item item) {
        boolean enable = !user.isAutoStore(item);

        setAutoStore(user, item, enable);

        return enable;
    }

    public void setAutoStoreAll(@NotNull User user, boolean enable) {
        user.getStocks().forEach(s -> s.setAutoStore(enable));

        plugin.getStorage().saveUser(user);
    }
}
