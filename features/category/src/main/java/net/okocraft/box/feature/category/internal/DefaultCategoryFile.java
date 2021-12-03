package net.okocraft.box.feature.category.internal;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import net.okocraft.box.api.BoxProvider;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public final class DefaultCategoryFile {

    public static void copy(@NotNull Path dist) throws IOException {
        ResourceUtils.copyFromJarIfNotExists(BoxProvider.get().getJar(), getFilename(), dist);
    }

    @SuppressWarnings("deprecation")
    private static @NotNull String getFilename() {
        var id = Bukkit.getUnsafe().getDataVersion();
        String version;

        if (id <= 2730) { // ~ 1.17.x
            version = "1_17";
        } else { // 1.18 ~
            version = "1_18";
        }

        return "categories_" + version + ".yml";
    }
}
