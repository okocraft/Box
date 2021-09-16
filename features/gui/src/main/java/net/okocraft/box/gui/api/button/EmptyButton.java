package net.okocraft.box.gui.api.button;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

record EmptyButton(Material material, int slot) implements Button {

    @Override
    public @NotNull Material getIconMaterial() {
        return material;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Contract("_, _ -> param2")
    @Override
    public @NotNull ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(Component.empty());
        target.lore(Collections.emptyList());

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
    }
}
