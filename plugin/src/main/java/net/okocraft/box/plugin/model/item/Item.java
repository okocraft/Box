package net.okocraft.box.plugin.model.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Box に登録されたアイテムをラップするクラス。
 */
public class Item {

    private final int internalID;
    private final ItemStack original;
    private String name;
    private boolean isCustomizedName = false;

    public Item(int internalID, @NotNull ItemStack original) {
        this(internalID, original, null);
    }

    public Item(int internalID, @NotNull ItemStack original, @Nullable String customName) {
        this.internalID = internalID;
        this.original = original;

        if (customName != null) {
            this.name = customName;
            isCustomizedName = true;
        } else {
            this.name = getDefaultName();
        }
    }

    public int getInternalID() {
        return internalID;
    }

    /**
     * 元の {@link ItemStack} をコピーして返す。
     *
     * @return コピーした元のアイテム
     */
    @NotNull
    public ItemStack getOriginalCopy() {
        return original.clone();
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setCustomName(@NotNull String customName) {
        if (!this.name.equals(customName)) {
            this.name = customName;
            isCustomizedName = !getDefaultName().equals(customName);
        }
    }

    public boolean isCustomizedName() {
        return isCustomizedName;
    }

    @NotNull
    private String getDefaultName() {
        if (original.hasItemMeta()) {
            return original.getType().name() + ":" + internalID;
        } else {
            return original.getType().name();
        }
    }
}
