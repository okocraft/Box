package net.okocraft.box.bundle;

import dev.siroshun.mcmsgdef.file.PropertiesFile;
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
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLDatabase;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLSetting;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteSetting;
import net.okocraft.box.storage.implementation.yaml.YamlStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public final class Builtin {

    public static void features(@NotNull BoxBootstrapContext context) {
        context.addFeature(CommandFeature::new)
            .addFeature(CategoryFeature::new)
            .addFeature(GuiFeature::new)
            .addFeature(BEModeFeature::new)
            .addFeature(AutoStoreFeature::new)
            .addFeature(CraftFeature::new)
            .addFeature(StickFeature::new)
            .addFeature(NotifierFeature::new);
    }

    public static void storages(@NotNull StorageRegistry registry) {
        registry.register(YamlStorage.STORAGE_NAME, YamlStorage.Setting.class, YamlStorage::new);
        registry.register("sqlite", SQLiteSetting.class, SQLiteDatabase::createStorage);
        registry.register("mysql", MySQLSetting.class, MySQLDatabase::createStorage);

        registry.setDefaultStorageName(YamlStorage.STORAGE_NAME);
    }

    public static void japaneseFile(@NotNull BoxBootstrapContext context) {
        context.addLocalization(Locale.JAPANESE, Builtin::loadJapaneseFileFromJar);
    }

    @VisibleForTesting
    public static @NotNull Map<String, String> loadJapaneseFileFromJar() {
        try (var input = Builtin.class.getClassLoader().getResourceAsStream("ja.properties")) {
            return input != null ? PropertiesFile.load(input) : Collections.emptyMap();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Builtin() {
        throw new UnsupportedOperationException();
    }
}
