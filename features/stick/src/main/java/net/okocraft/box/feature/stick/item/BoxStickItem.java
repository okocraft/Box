package net.okocraft.box.feature.stick.item;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A record to hold a key representing that the item is a Box Stick and provide methods to check if the {@link ItemStack} is Box Item.
 */
public final class BoxStickItem {

    private final @NotNull NamespacedKey key;
    private Consumer<Player> onRightClick;

    /**
     * The constructor of {@link BoxStickItem}.
     *
     * @param key the {@link NamespacedKey} to use for representing that the item is a Box Stick
     */
    @ApiStatus.Internal
    public BoxStickItem(@NotNull NamespacedKey key) {
        this.key = key;
    }

    /**
     * Saves the {@link #key()} to {@link PersistentDataContainer}
     *
     * @param target the target {@link PersistentDataContainer}
     */
    public void saveBoxStickKey(@NotNull PersistentDataContainer target) {
        target.set(this.key, PersistentDataType.BYTE, (byte) 1);
    }

    /**
     * Checks if the {@link ItemStack} is a Box Stick.
     *
     * @param itemStack the {@link ItemStack} to check
     * @return if the {@link ItemStack} has {@link #key()} in {@link PersistentDataContainer}, returns {@code true}, otherwise {@code false}
     */
    public boolean check(@NotNull ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().has(this.key);
    }

    /**
     * Gets the {@link NamespacedKey} that is used for saving data to {@link PersistentDataContainer}.
     *
     * @return the {@link NamespacedKey} that is used for saving data to {@link PersistentDataContainer}
     */
    public @NotNull NamespacedKey key() {
        return this.key;
    }

    /**
     * Sets the action called when the {@link Player} right-clicked.
     *
     * @param onRightClick the action on right-click
     */
    public void onRightClick(@NotNull Consumer<Player> onRightClick) {
        this.onRightClick = onRightClick;
    }

    /**
     * Calls the action of right-click
     *
     * @param player the {@link Player} who clicked
     */
    public void onRightClick(@NotNull Player player) {
        if (this.onRightClick != null) {
            this.onRightClick.accept(player);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BoxStickItem) obj;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key);
    }

    @Override
    public String toString() {
        return "BoxStickItem[" +
            "key=" + this.key +
            ']';
    }
}
