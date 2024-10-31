package net.okocraft.box.test.shared.mock.bukkit.inventory;

import dev.siroshun.configapi.core.serialization.annotation.CollectionType;
import dev.siroshun.configapi.core.serialization.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record InventoryInfo(int size,
                            @Key("initial") @CollectionType(InventoryItem.class) List<InventoryItem> initialItems,
                            @Key("result") @CollectionType(InventoryItem.class) List<InventoryItem> resultItems) {

    public @NotNull ContentsHoldingInventory createTestInventory() {
        return ContentsHoldingInventory.create(toContents(this.size, this.initialItems));
    }

    public void checkContents(@NotNull ContentsHoldingInventory inventory) {
        inventory.checkContents(toContents(this.size, this.resultItems));
    }

    private static @Nullable ItemStack @NotNull [] toContents(int size, List<InventoryItem> items) {
        var contents = new ItemStack[size];

        for (var item : items) {
            contents[item.position()] = item.toItemStack();
        }

        return contents;
    }
}
