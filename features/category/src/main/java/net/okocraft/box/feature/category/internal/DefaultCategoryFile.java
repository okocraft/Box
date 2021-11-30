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
        var prefix = "categories_";
        String filename;

        if (id <= 2730) { // ~ 1.17.x
            filename = prefix + "1_17";
        } else { // 1.18 ~
            filename = prefix + "1_18";
        }

        return filename + ".yml";
    }
}
