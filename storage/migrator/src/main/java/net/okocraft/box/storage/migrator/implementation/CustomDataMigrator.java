package net.okocraft.box.storage.migrator.implementation;

import com.github.siroshun09.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import org.jetbrains.annotations.NotNull;

public class CustomDataMigrator extends AbstractDataMigrator<ItemMigrator.Result, CustomDataStorage> {

    private static DataFixer dataFixer;

    public static void addDataFixer(@NotNull DataFixer dataFixer) {
        if (CustomDataMigrator.dataFixer != null) {
            CustomDataMigrator.dataFixer = CustomDataMigrator.dataFixer.append(dataFixer);
        } else {
            CustomDataMigrator.dataFixer = dataFixer;
        }
    }

    private final ItemMigrator.Result itemMigratorResult;

    public CustomDataMigrator(@NotNull ItemMigrator.Result itemMigratorResult) {
        this.itemMigratorResult = itemMigratorResult;
    }

    @Override
    protected @NotNull CustomDataStorage getDataStorage(@NotNull Storage storage) {
        return storage.getCustomDataStorage();
    }

    @Override
    protected @NotNull ItemMigrator.Result migrateData(@NotNull CustomDataStorage source, @NotNull CustomDataStorage target, boolean debug) throws Exception {
        source.visitAllData((key, mapNode) -> {
            try {
                target.saveData(key, dataFixer != null ? dataFixer.fix(key, mapNode, this.itemMigratorResult) : mapNode);
            } catch (Exception e) {
                sneakyThrow(e);
            }

            if (debug) {
                BoxLogger.logger().info("Migrated custom data ({}): {}", key, mapNode);
            }
        });
        return this.itemMigratorResult;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(@NotNull Throwable exception) throws T {
        throw (T) exception;
    }

    public interface DataFixer {

        @NotNull MapNode fix(@NotNull Key key, @NotNull MapNode data, @NotNull ItemMigrator.Result itemMigrationResult);

        default @NotNull DataFixer append(@NotNull DataFixer next) {
            return (key, data, itemMigrationResult) -> next.fix(key, this.fix(key, data, itemMigrationResult), itemMigrationResult);
        }
    }
}
