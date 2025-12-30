package net.okocraft.box.test.shared.mock.bukkit.inventory;

import net.okocraft.box.test.shared.mock.bukkit.item.ItemStackMock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.util.Arrays;

public abstract class ContentsHoldingInventory implements Inventory {

    public static @NotNull ContentsHoldingInventory create(int size) {
        return createMock(new ItemStack[size]);
    }

    public static @NotNull ContentsHoldingInventory create(@Nullable ItemStack @NotNull [] contents) {
        return createMock(Arrays.copyOf(contents, contents.length));
    }

    private static @NotNull ContentsHoldingInventory createMock(@Nullable ItemStack @NotNull [] contents) {
        ContentsHoldingInventory mock = Mockito.mock(ContentsHoldingInventory.class);

        Mockito.doCallRealMethod().when(mock).getContents();
        Mockito.doCallRealMethod().when(mock).getStorageContents();
        Mockito.doCallRealMethod().when(mock).setContents(Mockito.any());
        Mockito.doCallRealMethod().when(mock).setStorageContents(Mockito.any());
        Mockito.doCallRealMethod().when(mock).checkContents(Mockito.any());

        mock.contents = contents;
        return mock;
    }

    ItemStack[] contents;

    @Override
    public @Nullable ItemStack @NotNull [] getContents() {
        if (this.contents == null) {
            throw new IllegalStateException("Not initialized.");
        }
        return Arrays.copyOf(this.contents, this.contents.length);
    }

    @Override
    public @Nullable ItemStack @NotNull [] getStorageContents() {
        return this.getContents();
    }

    @Override
    public void setContents(@Nullable ItemStack @NotNull [] newContents) throws IllegalArgumentException {
        if (this.contents == null) {
            throw new IllegalStateException("Not initialized.");
        }

        if (this.contents.length != newContents.length) {
            throw new IllegalArgumentException("The number of new items does not match the size of this inventory.");
        }

        this.contents = Arrays.copyOf(newContents, newContents.length);
    }

    @Override
    public void setStorageContents(@Nullable ItemStack @NotNull [] newContents) throws IllegalArgumentException {
        this.setContents(newContents);
    }

    public void checkContents(@Nullable ItemStack @NotNull [] expectedContents) {
        ItemStack[] actualContents = this.contents;

        if (actualContents == null) {
            throw new IllegalStateException("Not initialized.");
        }

        Assertions.assertEquals(expectedContents.length, actualContents.length);

        for (int i = 0; i < expectedContents.length; i++) {
            ItemStack expectedItem = expectedContents[i];
            ItemStack actualItem = actualContents[i];

            if (expectedItem == null) {
                Assertions.assertNull(actualItem);
            } else {
                Assertions.assertNotNull(actualItem);
                ItemStackMock.checkSameTypeAndSameAmount(expectedItem, actualItem);
            }
        }
    }
}
