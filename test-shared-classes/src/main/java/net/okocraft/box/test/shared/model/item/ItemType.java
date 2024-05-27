package net.okocraft.box.test.shared.model.item;

import com.github.siroshun09.configapi.core.serialization.annotation.DefaultInt;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.test.shared.mock.bukkit.item.ItemStackMock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemType(Material item, @DefaultInt(64) int maxStackSize) {

    public @NotNull ItemStack toItemStack(int amount) {
        return ItemStackMock.createItemStack(this.item, amount, this.maxStackSize);
    }

    public @NotNull BoxItem asBoxItem(int id) {
        return new FakeBoxItem(this, id);
    }

    private record FakeBoxItem(@NotNull ItemType type, int id) implements BoxItem {
        @Override
        public int getInternalId() {
            return this.id;
        }

        @Override
        public @NotNull String getPlainName() {
            return ItemNameGenerator.key(this.type.item);
        }

        @Override
        public @NotNull ItemStack getOriginal() {
            return this.type.toItemStack(1);
        }

        @Override
        public @NotNull ItemStack getClonedItem() {
            return this.type.toItemStack(1);
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.translatable(this.type.item);
        }
    }
}
