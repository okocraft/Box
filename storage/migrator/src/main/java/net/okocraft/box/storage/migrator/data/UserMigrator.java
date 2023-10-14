package net.okocraft.box.storage.migrator.data;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.migrator.StorageMigrator;
import net.okocraft.box.storage.migrator.util.LoggerWrapper;
import net.okocraft.box.storage.migrator.util.MigratedBoxUsers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class UserMigrator implements DataMigrator<UserStorage> {


    @Override
    public @NotNull UserStorage getDataStorage(@NotNull Storage storage) {
        return storage.getUserStorage();
    }

    @Override
    public void migrate(@NotNull UserStorage source, @NotNull UserStorage target, @NotNull LoggerWrapper logger) throws Exception {
        var users = source.getAllUsers();
        var migratedUsers = new ArrayList<BoxUser>(users.size());

        for (var user : users) {
            try {
                target.saveBoxUser(user.getUUID(), user.getName().orElse(null));
            } catch (Exception e) {
                logger.warning("Failed to migrate the user: " + users + " (message: " + e.getMessage() + ")");
                continue;
            }

            migratedUsers.add(user);

            if (StorageMigrator.debug) {
                logger.info("Migrated user: " + user);
            }
        }

        logger.info(migratedUsers.size() + " users are migrated.");
        MigratedBoxUsers.LIST = Collections.unmodifiableList(migratedUsers);
    }
}
