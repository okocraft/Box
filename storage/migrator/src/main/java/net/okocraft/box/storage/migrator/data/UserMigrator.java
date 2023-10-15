package net.okocraft.box.storage.migrator.data;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import net.okocraft.box.storage.migrator.util.MigratedBoxUsers;
import org.jetbrains.annotations.NotNull;

public class UserMigrator implements DataMigrator<UserStorage> {


    @Override
    public @NotNull UserStorage getDataStorage(@NotNull Storage storage) {
        return storage.getUserStorage();
    }

    @Override
    public void migrate(@NotNull UserStorage source, @NotNull UserStorage target, @NotNull LoggerWrapper logger) throws Exception {
        var users = source.loadAllBoxUsers();
        target.saveBoxUsers(users);
        logger.info(users.size() + " users are migrated.");
        MigratedBoxUsers.LIST = users;
    }
}
