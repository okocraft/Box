package net.okocraft.box.test.shared.mock.bukkit.item;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemStackMock {

    private static final Map<Key, Material> MATERIAL_MOCK_MAP = new ConcurrentHashMap<>();

    public static @NotNull ItemStack createItemStack(@NotNull Material item, int amount, int maxStackSize) {
        if (maxStackSize < 1) {
            throw new IllegalArgumentException("Invalid max stack size: " + amount);
        }

        if (amount < 1 || maxStackSize < amount) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }

        var mockedMaterial = MATERIAL_MOCK_MAP.computeIfAbsent(item.key(), key -> {
            var mocked = Mockito.mock(Material.class);
            Mockito.when(mocked.key()).thenReturn(key);
            Mockito.when(mocked.isAir()).thenReturn(item == Material.AIR);
            Mockito.when(mocked.getMaxStackSize()).thenReturn(maxStackSize);
            return mocked;
        });

        var mockedItem = Mockito.mock(ItemStack.class);

        Mockito.when(mockedItem.getType()).thenReturn(mockedMaterial);
        Mockito.when(mockedItem.getAmount()).thenReturn(amount);
        Mockito.when(mockedItem.getMaxStackSize()).thenReturn(maxStackSize);
        Mockito.when(mockedItem.asQuantity(Mockito.anyInt())).thenAnswer(invocation -> createItemStack(item, invocation.getArgument(0), maxStackSize));
        Mockito.when(mockedItem.isSimilar(Mockito.any())).thenAnswer(invocation -> mockedItem.getType().key().equals(invocation.<ItemStack>getArgument(0).getType().key()));

        return mockedItem;
    }

    public static void checkSameTypeAndSameAmount(@NotNull ItemStack item1, @NotNull ItemStack item2) {
        checkSameType(item1, item2);
        Assertions.assertEquals(item1.getAmount(), item2.getAmount());
    }

    public static void checkSameType(@NotNull ItemStack item1, @NotNull ItemStack item2) {
        Assertions.assertEquals(item1.getType().key(), item2.getType().key());
    }

    private ItemStackMock() {
        throw new UnsupportedOperationException();
    }
}
