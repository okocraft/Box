package net.okocraft.box.api.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A utility class for an {@link Inventory}.
 */
public final class InventoryUtil {

    /**
     * Puts items in the {@link Inventory}.
     *
     * @param inventory an {@link Inventory} to put items in
     * @param item      {@link ItemStack} to put
     * @param amount    amount of items to put
     * @return amount of items that could not be put in the {@link Inventory} because it was full
     * @throws NullPointerException     if {@code inventory} or {@code item} is null
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public static int putItems(@NotNull Inventory inventory, @NotNull ItemStack item, int amount) {
        Objects.requireNonNull(inventory);
        Objects.requireNonNull(item);

        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }

        int remaining = amount;
        int maxStackSize = item.getMaxStackSize();

        ItemStack[] contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length && 0 < remaining; i++) {
            ItemStack content = contents[i];

            if (content == null) {
                if (remaining < maxStackSize) {
                    contents[i] = item.asQuantity(remaining);
                    remaining = 0;
                } else {
                    contents[i] = item.asQuantity(maxStackSize);
                    remaining -= maxStackSize;
                }
            } else if (content.isSimilar(item)) {
                int current = content.getAmount();
                int cap = maxStackSize - current;

                if (cap <= 0) {
                    continue;
                }

                if (remaining < cap) {
                    contents[i] = item.asQuantity(current + remaining);
                    remaining = 0;
                } else {
                    contents[i] = item.asQuantity(maxStackSize);
                    remaining -= cap;
                }
            }
        }

        if (remaining != amount) {
            inventory.setStorageContents(contents);
        }

        return remaining;
    }

    private InventoryUtil() {
        throw new UnsupportedOperationException();
    }
}
