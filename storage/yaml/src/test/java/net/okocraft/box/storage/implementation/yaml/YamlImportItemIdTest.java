package net.okocraft.box.storage.implementation.yaml;

import net.okocraft.box.storage.api.model.item.DefaultItemData;
import net.okocraft.box.storage.api.model.item.ItemData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class YamlImportItemIdTest {
    @TempDir Path root;

    @Test
    void importedIdsSurviveAndNextIdIsAboveTheHighest() throws Exception {
        Path fresh = this.root.resolve("fresh");
        var meta = new YamlMetaStorage(fresh);
        var defaults = new YamlDefaultItemStorage(fresh, meta);
        var customs = new YamlCustomItemStorage(fresh, meta);
        defaults.saveDefaultItems(List.of(new DefaultItemData(40, "stone")));
        customs.saveCustomItems(List.of(new ItemData(15, "custom", new byte[]{1, 2})));

        assertEquals(40, defaults.loadDefaultItemNameToIdMap().getInt("stone"));
        List<ItemData> loaded = new ArrayList<>();
        customs.loadItemData(loaded::add);
        assertEquals(1, loaded.size());
        assertEquals(15, loaded.getFirst().internalId());
        assertArrayEquals(new byte[]{1, 2}, loaded.getFirst().itemData());
        assertEquals(41, meta.newItemId());
    }
}
