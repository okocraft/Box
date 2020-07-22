package net.okocraft.box.plugin.database;

import net.okocraft.box.plugin.database.connector.Database;
import net.okocraft.box.plugin.database.table.ItemTable;
import net.okocraft.box.plugin.database.table.MasterTable;
import net.okocraft.box.plugin.database.table.PlayerTable;
import net.okocraft.box.plugin.model.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

public class Storage {

    private final Database database;
    private final PlayerTable playerTable;
    private final ItemTable itemTable;
    private final MasterTable masterTable;

    private final Set<Item> items;

    public Storage(@NotNull Database.Type type) { // TODO
        database = Database.connectSQLite(Path.of("./plugins/Box/data.db"));
        database.start();

        playerTable = new PlayerTable(database, "box_");
        itemTable = new ItemTable(database, "box_");

        try {
            items = itemTable.loadAllItems();
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }

        masterTable = new MasterTable(database, this, "box_");
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
