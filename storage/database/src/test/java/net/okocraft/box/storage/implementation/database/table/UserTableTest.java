package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.test.shared.storage.test.UserStorageTest;
import org.jetbrains.annotations.NotNull;

public abstract class UserTableTest extends UserStorageTest<Database> {
    @Override
    protected @NotNull UserStorage newUserStorage(@NotNull Database database) throws Exception {
        var table = new UserTable(database);

        try (var connection = database.getConnection()) {
            table.init(connection);
        }

        return table;
    }
}
