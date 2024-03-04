package net.okocraft.box.test.shared.mock.bukkit.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public final class ItemStackMock {

    public static @NotNull ItemStack createItemStack(@NotNull Material item, int amount) {
        if (amount < 1 || item.getMaxStackSize() < amount) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }

        var mock = Mockito.mock(ItemStack.class);

        Mockito.when(mock.getType()).thenReturn(item);
        Mockito.when(mock.getAmount()).thenReturn(amount);
        Mockito.when(mock.getMaxStackSize()).thenReturn(item.getMaxStackSize());
        Mockito.when(mock.asQuantity(Mockito.anyInt())).thenAnswer(invocation -> createItemStack(item, invocation.getArgument(0)));
        Mockito.when(mock.isSimilar(Mockito.any())).thenAnswer(invocation -> mock.getType() == invocation.<ItemStack>getArgument(0).getType());

        return mock;
    }

    public static void checkSameTypeAndSameAmount(@NotNull ItemStack item1, @NotNull ItemStack item2) {
        checkSameType(item1, item2);
        Assertions.assertEquals(item1.getAmount(), item2.getAmount());
    }

    public static void checkSameType(@NotNull ItemStack item1, @NotNull ItemStack item2) {
        Assertions.assertEquals(item1.getType(), item2.getType());
    }

    private ItemStackMock() {
        throw new UnsupportedOperationException();
    }
}
