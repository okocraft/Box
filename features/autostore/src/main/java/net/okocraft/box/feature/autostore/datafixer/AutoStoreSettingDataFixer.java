package net.okocraft.box.feature.autostore.datafixer;

import com.github.siroshun09.configapi.core.node.IntArray;
import com.github.siroshun09.configapi.core.node.ListNode;
import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.node.NumberValue;
import net.kyori.adventure.key.Key;
import net.okocraft.box.storage.migrator.implementation.CustomDataMigrator;
import net.okocraft.box.storage.migrator.implementation.ItemMigrator;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public class AutoStoreSettingDataFixer implements CustomDataMigrator.DataFixer {
    @Override
    public @NotNull MapNode fix(@NotNull Key key, @NotNull MapNode data, ItemMigrator.@NotNull Result itemMigrationResult) {
        if (!key.namespace().equals("autostore")) {
            return data;
        }

        IntStream idStream;

        var enabledItemsNode = data.get("enabled-items");

        if (enabledItemsNode instanceof IntArray array) {
            idStream = IntStream.of(array.value());
        } else if (enabledItemsNode instanceof ListNode list) {
            idStream = list.asList(NumberValue.class).stream().mapToInt(NumberValue::asInt);
        } else {
            return data;
        }

        data.set("enabled-items", idStream.map(id -> itemMigrationResult.itemIdMap().getOrDefault(id, id)).toArray());

        return data;
    }
}
