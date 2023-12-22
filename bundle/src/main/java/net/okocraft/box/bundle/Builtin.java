package net.okocraft.box.bundle;

import com.github.siroshun09.messages.api.util.PropertiesFile;
import net.okocraft.box.bootstrap.BoxBootstrapContext;
import net.okocraft.box.feature.autostore.AutoStoreFeature;
import net.okocraft.box.feature.bemode.BEModeFeature;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.command.CommandFeature;
import net.okocraft.box.feature.craft.CraftFeature;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.notifier.NotifierFeature;
import net.okocraft.box.feature.stick.StickFeature;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLDatabase;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLSetting;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteSetting;
import net.okocraft.box.storage.implementation.yaml.YamlStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Locale;

public final class Builtin {

    public static void features(@NotNull BoxBootstrapContext context) {
        context.addFeature(CommandFeature::new)
                .addFeature(ignored -> new CategoryFeature())
                .addFeature(ignored -> new GuiFeature())
                .addFeature(ignored -> new BEModeFeature())
                .addFeature(ignored -> new AutoStoreFeature())
                .addFeature(ignored -> new CraftFeature())
                .addFeature(ignored -> new StickFeature())
                .addFeature(ignored -> new NotifierFeature());
    }

    public static void storages(@NotNull StorageRegistry registry) {
        registry.register(YamlStorage.STORAGE_NAME, YamlStorage.Setting.class, YamlStorage::new);
        registry.register(Database.Type.SQLITE.getName(), SQLiteSetting.class, SQLiteDatabase::createStorage);
        registry.register(Database.Type.MYSQL.getName(), MySQLSetting.class, MySQLDatabase::createStorage);

        registry.setDefaultStorageName(YamlStorage.STORAGE_NAME);
    }

    public static void japaneseFile(@NotNull BoxBootstrapContext context) {
        context.addLocalization(Locale.JAPANESE, () -> {
            try (var input = Builtin.class.getResourceAsStream("ja.properties")) {
                return input != null ? PropertiesFile.load(input) : Collections.emptyMap();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private Builtin() {
        throw new UnsupportedOperationException();
    }
}
