package net.okocraft.box.feature.stick.function.container;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ContainerContents {

    public static @Nullable ItemStack @NotNull [] copy(@Nullable ItemStack @NotNull [] contents) {
        var copy = contents.clone();
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] != null) {
                copy[i] = copy[i].clone();
            }
        }
        return copy;
    }

    public static boolean hasNetChange(@Nullable ItemStack @NotNull [] before, @Nullable ItemStack @NotNull [] after) {
        if (Arrays.equals(before, after)) {
            return false;
        }

        // A move between slots or a split stack should not count as adding or removing items.
        Map<ItemStack, Long> deltas = new HashMap<>();
        accumulate(deltas, before, -1);
        accumulate(deltas, after, 1);
        return deltas.values().stream().anyMatch(delta -> delta != 0);
    }

    private static void accumulate(Map<ItemStack, Long> deltas, @Nullable ItemStack @NotNull [] contents, int sign) {
        for (var item : contents) {
            if (item != null && !item.isEmpty()) {
                deltas.merge(item.asOne(), (long) sign * item.getAmount(), Long::sum);
            }
        }
    }

    private ContainerContents() {
    }
}
