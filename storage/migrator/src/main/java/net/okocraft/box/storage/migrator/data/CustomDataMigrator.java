package net.okocraft.box.storage.migrator.data;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.migrator.StorageMigrator;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class CustomDataMigrator implements DataMigrator<CustomDataStorage> {

    @Override
    public @NotNull CustomDataStorage getDataStorage(@NotNull Storage storage) {
        return storage.getCustomDataStorage();
    }

    @Override
    public void migrate(@NotNull CustomDataStorage source, @NotNull CustomDataStorage target, @NotNull Logger logger) throws Exception {
        for (var key : source.getKeys()) {
            var data = source.load(key.namespace(), key.key());
            target.save(key.namespace(), key.key(), data);

            if (StorageMigrator.debug) {
                logger.info("Migrated custom data (" + key + "): " + data);
            }
        }
    }
}
