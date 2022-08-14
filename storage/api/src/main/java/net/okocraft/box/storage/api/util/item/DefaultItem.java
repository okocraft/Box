package net.okocraft.box.storage.api.util.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record DefaultItem(@NotNull String plainName, @NotNull ItemStack itemStack) {
}
