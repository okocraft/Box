package net.okocraft.box.bundle;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.feature.autostore.AutoStoreFeature;
import net.okocraft.box.feature.bemode.BEModeFeature;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.command.CommandFeature;
import net.okocraft.box.feature.craft.CraftFeature;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.notifier.NotifierFeature;
import net.okocraft.box.feature.stick.StickFeature;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.implementation.database.DatabaseStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import net.okocraft.box.storage.implementation.yaml.YamlStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class Bundled {

    private static final List<BoxFeature> FEATURES =
            List.of(new CommandFeature(), new CategoryFeature(), new GuiFeature(),
                    new BEModeFeature(), new AutoStoreFeature(), new CraftFeature(),
                    new StickFeature(), new NotifierFeature());

    static @NotNull @Unmodifiable List<BoxFeature> features() {
        return FEATURES;
    }

    static @NotNull @Unmodifiable Map<String, Function<Configuration, Storage>> storageMap() {
        return Map.of(
                YamlStorage.STORAGE_NAME, Bundled::createYamlStorage,
                Database.Type.SQLITE.getName(), Bundled::createSQLiteStorage
        );
    }

    private static @NotNull Storage createYamlStorage(@NotNull Configuration config) {
        var dirName = config.getString("yaml.directory-name", "data");
        return new YamlStorage(BoxProvider.get().getPluginDirectory().resolve(dirName));
    }

    private static @NotNull Storage createSQLiteStorage(@NotNull Configuration config) {
        return new DatabaseStorage(
                new SQLiteDatabase(
                        BoxProvider.get().getPluginDirectory().resolve(config.getString("sqlite.filename", "box-sqlite.db")),
                        config.getString("database.table-prefix", "box_")
                )
        );
    }
}
