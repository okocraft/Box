package net.okocraft.box.version.common.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record DefaultItem(@NotNull String plainName, @NotNull ItemStack itemStack) {
}
