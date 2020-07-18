package net.okocraft.box.plugin.model.item;

import org.jetbrains.annotations.NotNull;

/**
 * アイテムの所持数を管理するクラス
 */
public class Stock {

    private final int internalID;
    private final Item item;
    private int amount;
    private boolean autoStore;

    public Stock(int internalID, @NotNull Item item) {
        this(internalID, item, 0, false);
    }

    public Stock(int internalID, @NotNull Item item, int amount, boolean autoStore) {
        this.internalID = internalID;
        this.item = item;
        this.amount = amount;
        this.autoStore = autoStore;
    }

    public int getInternalID() {
        return internalID;
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
