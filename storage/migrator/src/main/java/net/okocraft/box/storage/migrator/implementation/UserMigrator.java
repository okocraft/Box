package net.okocraft.box.storage.migrator.implementation;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class UserMigrator extends AbstractDataMigrator<UserMigrator.Result, UserStorage> {

    @Override
    public @NotNull UserStorage getDataStorage(@NotNull Storage storage) {
        return storage.getUserStorage();
    }

    @Override
    public @NotNull UserMigrator.Result migrateData(@NotNull UserStorage source, @NotNull UserStorage target, boolean debug) throws Exception {
        var users = source.loadAllBoxUsers();
        target.saveBoxUsers(users);
        BoxLogger.logger().info("{} users are migrated.", users.size());
        return new Result(users);
    }

    public record Result(@NotNull Collection<BoxUser> users) {
    }
}
