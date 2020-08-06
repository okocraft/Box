package net.okocraft.box.plugin.gui.builder;

import net.okocraft.box.plugin.gui.ItemList;
import net.okocraft.box.plugin.gui.item.AbstractMenuItem;
import net.okocraft.box.plugin.gui.item.MenuItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FlameBuilder {

    private final ItemStack item;
    private final List<Integer> positions;

    private FlameBuilder(@NotNull ItemStack item) {
        this.item = item;
        this.positions = new LinkedList<>();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull FlameBuilder of(@NotNull ItemStack item) {
        return new FlameBuilder(item);
    }

    public FlameBuilder addRange(int start, int end) {
        while (start < end) {
            positions.add(start);
            start++;
        }

        return this;
    }

    public FlameBuilder add(int index) {
        positions.add(index);
        return this;
    }

    public FlameBuilder add(int index1, int index2) {
        positions.add(index1);
        positions.add(index2);
        return this;
    }

    public FlameBuilder add(Integer... indexes) {
        positions.addAll(Arrays.asList(indexes));
        return this;
    }

    public void apply(@NotNull Set<MenuItem> itemSet) {
        for (int index : positions) {
            itemSet.add(new Flame(item, index));
        }
    }

    public void apply(@NotNull ItemList itemList) {
        for (int index : positions) {
            itemList.add(new Flame(item, index));
        }
    }

    private static class Flame extends AbstractMenuItem {
        private Flame(@NotNull ItemStack item, int index) {
            super(item, index);
        }
    }
}
