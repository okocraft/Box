package net.okocraft.box.test.shared.mock.bukkit.inventory;

import com.github.siroshun09.configapi.core.serialization.annotation.DefaultInt;
import com.github.siroshun09.configapi.core.serialization.annotation.Inline;
import net.okocraft.box.test.shared.model.item.ItemType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record InventoryItem(@Inline ItemType item, @DefaultInt(1) int amount, int position) {
    public @NotNull ItemStack toItemStack() {
        return this.item.toItemStack(this.amount);
    }
}
