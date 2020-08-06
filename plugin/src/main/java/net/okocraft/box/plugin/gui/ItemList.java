package net.okocraft.box.plugin.gui;

import net.okocraft.box.plugin.gui.item.MenuItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemList {

    private final Set<MenuItem> itemSet = new HashSet<>();

    public void add(@NotNull MenuItem item) {
        itemSet.stream()
                .filter(i -> i.getIndex() == item.getIndex())
                .forEach(itemSet::remove); // これは CME でるのか?

        itemSet.add(item);
    }

    public void apply(@NotNull Inventory inv) {
        for (MenuItem item : itemSet) {
            inv.setItem(item.getIndex(), item.getItem());
        }
    }

    public void click(@NotNull InventoryClickEvent e) {
        itemSet.stream().filter(i -> i.getIndex() == e.getSlot()).findFirst().ifPresent(i -> i.onClick(e));
    }

    @NotNull
    @Unmodifiable
    public Set<Integer> getUsedIndexes() {
        return itemSet.stream().map(MenuItem::getIndex).collect(Collectors.toUnmodifiableSet());
    }
}
