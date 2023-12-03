package net.okocraft.box.feature.category.internal.file;

import com.github.siroshun09.configapi.core.node.ListNode;
import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.feature.category.internal.category.CommonDefaultCategory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class BundledCategoryFile {

    static @NotNull MapNode loadDefaultCategoryFile() throws IOException {
        try (var input = BundledCategoryFile.class.getResourceAsStream(getFilename())) {
            if (input != null) {
                return YamlFormat.DEFAULT.load(input);
            } else {
                return MapNode.empty();
            }
        }
    }

    static @NotNull Map<Set<String>, CommonDefaultCategory> loadDefaultCategoryMap(@NotNull MapNode source) {
        Map<Set<String>, CommonDefaultCategory> result = new HashMap<>();

        for (var entry : source.value().entrySet()) {
            var key = String.valueOf(entry.getKey());

            if (key.equals("icons") ||
                    key.equals(CommonDefaultCategory.UNCATEGORIZED.getName()) ||
                    key.equals(CommonDefaultCategory.CUSTOM_ITEMS.getName()) ||
                    !(entry.getValue() instanceof ListNode listNode)) {
                continue;
            }

            var category = CommonDefaultCategory.byName(key);

            if (category != null) {
                result.put(Set.copyOf(listNode.asList(String.class)), category);
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
        } else if (currentVer.isAfterOrSame(MCDataVersion.MC_1_20)) { // 1.20
            version = "1_20";
        } else {
            // Future version? Use latest categories.yml
            version = "1_20";
        }

        return "categories_" + version + ".yml";
    }
}
