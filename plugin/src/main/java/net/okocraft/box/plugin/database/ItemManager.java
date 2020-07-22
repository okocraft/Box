package net.okocraft.box.plugin.database;

import net.okocraft.box.plugin.database.table.ItemTable;
import net.okocraft.box.plugin.model.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public class ItemManager {

    private  final ItemTable table;
    private final Set<Item> items;

    public ItemManager(ItemTable table) {
        this.table = table;

        try {
            items = table.loadAllItems();
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public Optional<Item> getItemById(int id) {
       return items.stream().filter(i -> i.getInternalID() == id).findFirst();
    }

    @NotNull
    @Unmodifiable
    public Set<Item> getItems() {
        return Set.copyOf(items);
    }
}
