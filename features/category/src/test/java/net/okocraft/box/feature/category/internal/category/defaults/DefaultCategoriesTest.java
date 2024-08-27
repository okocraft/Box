package net.okocraft.box.feature.category.internal.category.defaults;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.feature.category.internal.category.defaults.DefaultCategories.ItemNameSet;
import net.okocraft.box.feature.category.internal.category.defaults.DefaultCategories.VersionedItemName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static net.okocraft.box.feature.category.internal.category.defaults.DefaultCategories.ItemNameSet.UNKNOWN_VERSION;
import static net.okocraft.box.feature.category.internal.category.defaults.DefaultCategories.collectCurrentDefaultCategories;
import static net.okocraft.box.feature.category.internal.category.defaults.DefaultCategories.collectNewItems;

class DefaultCategoriesTest {

    private static final String ITEM_LIST = """
        dirt:
          - DIRT
          - GRASS_BLOCK
        flowers:
          - GRASS;10:SHORT_GRASS
          - TALL_GRASS
        unavailable:
          - 10:NEW_ITEM
        """;

    @Test
    void testGetCurrentName() {
        Assertions.assertEquals(new ItemNameSet(UNKNOWN_VERSION, List.of(new VersionedItemName(UNKNOWN_VERSION, "GRASS"))), ItemNameSet.parse("GRASS"));
        Assertions.assertEquals(new ItemNameSet(MCDataVersion.of(5), List.of(new VersionedItemName(MCDataVersion.of(5), "GRASS"))), ItemNameSet.parse("5:GRASS"));
        Assertions.assertEquals(new ItemNameSet(UNKNOWN_VERSION, List.of(new VersionedItemName(UNKNOWN_VERSION, "GRASS"), new VersionedItemName(MCDataVersion.of(10), "SHORT_GRASS"))), ItemNameSet.parse("GRASS;10:SHORT_GRASS"));
        Assertions.assertEquals(new ItemNameSet(MCDataVersion.of(5), List.of(new VersionedItemName(MCDataVersion.of(5), "GRASS"), new VersionedItemName(MCDataVersion.of(10), "SHORT_GRASS"))), ItemNameSet.parse("5:GRASS;10:SHORT_GRASS"));
    }

    @Test
    void testCollectCurrentDefaultCategories() throws IOException {
        var source = DefaultCategories.loadCategorizedItemNames(YamlFormat.DEFAULT.load(new StringReader(ITEM_LIST)));

        {
            var categories = new ArrayList<>(collectCurrentDefaultCategories(MCDataVersion.of(10), source));
            categories.removeIf(category -> category.itemNames().isEmpty());
            Assertions.assertEquals(3, categories.size());
            Assertions.assertEquals(List.of("DIRT", "GRASS_BLOCK"), categories.get(0).itemNames());
            Assertions.assertEquals(List.of("SHORT_GRASS", "TALL_GRASS"), categories.get(1).itemNames());
            Assertions.assertEquals(List.of("NEW_ITEM"), categories.get(2).itemNames());
        }

        {
            var categories = new ArrayList<>(collectCurrentDefaultCategories(MCDataVersion.of(5), source));
            categories.removeIf(category -> category.itemNames().isEmpty());
            Assertions.assertEquals(2, categories.size());
            Assertions.assertEquals(List.of("DIRT", "GRASS_BLOCK"), categories.get(0).itemNames());
            Assertions.assertEquals(List.of("GRASS", "TALL_GRASS"), categories.get(1).itemNames());
        }
    }

    @Test
    void testCollectNewItems() throws IOException {
        var source = DefaultCategories.loadCategorizedItemNames(YamlFormat.DEFAULT.load(new StringReader(ITEM_LIST)));

        {
            var categories = new ArrayList<>(collectNewItems(MCDataVersion.of(5), MCDataVersion.of(10), source));
            categories.removeIf(category -> category.itemNames().isEmpty());
            Assertions.assertEquals(1, categories.size());
            Assertions.assertEquals(List.of("NEW_ITEM"), categories.get(0).itemNames());
        }

        {
            var categories = new ArrayList<>(collectNewItems(MCDataVersion.of(10), MCDataVersion.of(10), source));
            categories.removeIf(category -> category.itemNames().isEmpty());
            Assertions.assertTrue(categories.isEmpty());
        }

        {
            var categories = new ArrayList<>(collectNewItems(MCDataVersion.of(5), MCDataVersion.of(7), source));
            categories.removeIf(category -> category.itemNames().isEmpty());
            Assertions.assertTrue(categories.isEmpty());
        }
    }
}
