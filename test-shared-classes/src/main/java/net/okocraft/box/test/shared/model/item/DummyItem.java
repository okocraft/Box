package net.okocraft.box.test.shared.model.item;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BoxItem} that does not have a {@link ItemStack}.
 *
 * @param internalId the id
 * @param plainName  the name
 */
public record DummyItem(int internalId, @NotNull String plainName) implements BoxItem {

    @Override
    public int getInternalId() {
        return this.internalId;
    }

    @Override
    public @NotNull String getPlainName() {
        return this.plainName;
    }

    @Override
    public @NotNull ItemStack getOriginal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ItemStack getClonedItem() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text(this.plainName);
    }

    @Contract("_ -> new")
    public @NotNull DummyItem rename(@NotNull String newName) {
        return new DummyItem(this.internalId, newName);
    }
}
