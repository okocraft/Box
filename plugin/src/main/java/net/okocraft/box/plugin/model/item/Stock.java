package net.okocraft.box.plugin.model.item;

import org.jetbrains.annotations.NotNull;

/**
 * アイテムの所持数を管理するクラス
 */
public class Stock {

    private final Item item;
    private int amount;
    private boolean autoStore;

    public Stock(@NotNull Item item) {
        this(item, 0, false);
    }

    public Stock(@NotNull Item item, int amount, boolean autoStore) {
        this.item = item;
        this.amount = amount;
        this.autoStore = autoStore;
    }

    @NotNull
    public Item getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    // ここにあるべきなのだろうか?
    public boolean isAutoStore() {
        return autoStore;
    }

    // ここにあるべきなのだろうか?
    public void setAutoStore(boolean autoStore) {
        this.autoStore = autoStore;
    }
}
