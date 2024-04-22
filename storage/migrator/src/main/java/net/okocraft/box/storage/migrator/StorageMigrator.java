package net.okocraft.box.storage.migrator;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.migrator.implementation.CustomDataMigrator;
import net.okocraft.box.storage.migrator.implementation.DataMigrator;
import net.okocraft.box.storage.migrator.implementation.ItemMigrator;
import net.okocraft.box.storage.migrator.implementation.StockMigrator;
import net.okocraft.box.storage.migrator.implementation.UserMigrator;
import org.jetbrains.annotations.NotNull;

public class StorageMigrator implements AutoCloseable {

    private final Storage sourceStorage;
    private final Storage targetStorage;
    private final DefaultItemProvider defaultItemProvider;
    private final boolean debug;
    private boolean closed;

    public StorageMigrator(@NotNull Storage source, @NotNull Storage target, @NotNull DefaultItemProvider defaultItemProvider, boolean debug) {
        this.sourceStorage = source;
        this.targetStorage = target;
        this.defaultItemProvider = defaultItemProvider;
        this.debug = debug;
    }

    public void init() throws Exception {
        sourceStorage.init();
        targetStorage.init();
    }

    public void run() throws Exception {
        var base = this.createMigratorBase();
        if (base.checkRequirements(this.sourceStorage, this.targetStorage)) {
            base.createMigrator(null).migrate(this.sourceStorage, this.targetStorage, this.debug);
        }
    }

    @Override
    public void close() throws Exception {
        if (!this.closed) {
            this.closed = true;
            sourceStorage.close();
            targetStorage.close();
        }
    }

    private @NotNull DataMigrator.Base<Void, ItemMigrator.Result> createMigratorBase() {
        return UserMigrator.create()
                .next(ItemMigrator.create(this.defaultItemProvider))
                .next(StockMigrator.create())
                .next(CustomDataMigrator.create());
    }
}
