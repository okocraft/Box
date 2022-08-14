package net.okocraft.box.feature.category.internal;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.category.internal.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public final class BundledCategoryFile {

    public static void copy(@NotNull Path dist) throws IOException {
        ResourceUtils.copyFromJarIfNotExists(BoxProvider.get().getJar(), getFilename(), dist);
    }

    private static @NotNull String getFilename() {
        var currentVer = MCDataVersion.CURRENT;
        String version;

        if (currentVer.isBeforeOrSame(MCDataVersion.MC_1_17)) { // ~ 1.17.x
            version = "1_17";
        } else if (currentVer.isBeforeOrSame(MCDataVersion.MC_1_18)) { // ~ 1.18.x
            version = "1_18";
        } else {
            version = "1_19";
        }

        return "categories_" + version + ".yml";
    }
}
