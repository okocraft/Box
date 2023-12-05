package net.okocraft.box.feature.category.internal.file;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.feature.category.internal.category.CommonDefaultCategory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

public final class BundledCategoryFile {

    public static void copy(@NotNull Path dist) throws IOException {
        ResourceUtils.copyFromJarIfNotExists(BoxProvider.get().getJar(), getFilename(), dist);
    }

    static @NotNull Map<Set<String>, CommonDefaultCategory> loadDefaultCategoryMap() throws IOException {
        Configuration source;

        try (var jar = new JarFile(BoxProvider.get().getJar().toFile());
             var input = ResourceUtils.getInputStreamFromJar(jar, getFilename())) {
            source = YamlConfiguration.loadFromInputStream(input);
        }

        Map<Set<String>, CommonDefaultCategory> result = new HashMap<>();

        for (var key : source.getKeyList()) {
            if (key.equals("icons") ||
                    key.equals(CommonDefaultCategory.UNCATEGORIZED.getName()) ||
                    key.equals(CommonDefaultCategory.CUSTOM_ITEMS.getName())) {
                continue;
            }

            var category = CommonDefaultCategory.byName(key);

            if (category != null) {
                result.put(Set.copyOf(source.getStringList(key)), category);
            } else {
                BoxProvider.get().getLogger().warning("Unknown default category: " + key);
            }
        }

        return result;
    }

    private static @NotNull String getFilename() {
        var currentVer = MCDataVersion.CURRENT;
        String version;

        if (currentVer.isBeforeOrSame(MCDataVersion.MC_1_17_1)) { // ~ 1.17.1
            version = "1_17";
        } else if (currentVer.isBeforeOrSame(MCDataVersion.MC_1_18_2)) { // 1.18, 1.18.1, 1.18.2
            version = "1_18";
        } else if (currentVer.isBeforeOrSame(MCDataVersion.MC_1_19_2)) { // 1.19, 1.19.1, 1.19.2
            version = "1_19";
        } else if (currentVer.isBeforeOrSame(MCDataVersion.MC_1_19_4)) { // 1.19.3, 1.19.4
            version = "1_19_3";
        } else if (currentVer.isAfterOrSame(MCDataVersion.MC_1_20)) { // 1.20, 1.20.1, 1.20.2
            version = "1_20";
        } else if (currentVer.isAfterOrSame(MCDataVersion.MC_1_20_3)) { // 1.20.3
            version = "1_20_3";
        } else {
            // Future version? Use latest categories.yml
            version = "1_20_3";
        }

        return "categories_" + version + ".yml";
    }
}
