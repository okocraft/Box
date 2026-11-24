package net.okocraft.box.feature.stick.function.container;

import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContainerContentsTest {

    @Test
    void testSplittingAndMovingStacksDoesNotCountAsAChange() {
        var kind = mock(ItemStack.class);
        var before = new ItemStack[]{stack(kind, 5), null, null};
        var after = new ItemStack[]{null, stack(kind, 2), stack(kind, 3)};

        assertFalse(ContainerContents.hasNetChange(before, after));
    }

    @Test
    void testNetQuantityChangeIsDetected() {
        var kind = mock(ItemStack.class);

        assertTrue(ContainerContents.hasNetChange(
            new ItemStack[]{stack(kind, 5)}, new ItemStack[]{stack(kind, 4)}));
    }

    @Test
    void testCopyDetachesSlotsAndItems() {
        var item = mock(ItemStack.class);
        var cloned = mock(ItemStack.class);
        when(item.clone()).thenReturn(cloned);
        var contents = new ItemStack[]{item, null};

        var copy = ContainerContents.copy(contents);
        contents[0] = null;

        assertSame(cloned, copy[0]);
        assertNull(copy[1]);
    }

    private static ItemStack stack(ItemStack kind, int amount) {
        var item = mock(ItemStack.class);
        when(item.asOne()).thenReturn(kind);
        when(item.getAmount()).thenReturn(amount);
        return item;
    }
}
