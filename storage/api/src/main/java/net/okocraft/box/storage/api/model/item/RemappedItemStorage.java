package net.okocraft.box.storage.api.model.item;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.okocraft.box.api.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface RemappedItemStorage {

    @NotNull
    Map<MCDataVersion, Int2IntMap> loadRemappedIds() throws Exception;

    @NotNull
    Int2IntMap loadRemappedIds(@NotNull MCDataVersion version) throws Exception;

    void saveRemappedItem(int id, @NotNull String name, int remappedTo, @NotNull MCDataVersion inVersion) throws Exception;
}
