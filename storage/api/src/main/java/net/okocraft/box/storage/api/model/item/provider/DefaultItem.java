package net.okocraft.box.storage.api.model.item.provider;

import net.okocraft.box.storage.api.model.item.NamedItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record DefaultItem(@NotNull String plainName, @NotNull ItemStack itemStack) implements NamedItem<ItemStack> {
    @Override
    public @NotNull ItemStack item() {
        return this.itemStack;
    }
}
