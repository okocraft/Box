package net.okocraft.box.storage.api.model.item;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface CustomItemStorage {

    void loadItemData(@NotNull Consumer<ItemData> dataConsumer) throws Exception;

    void updateItemData(@NotNull Stream<ItemData> items) throws Exception;

    int newCustomItem(@NotNull String name, byte[] data) throws Exception;

    void renameCustomItem(int id, @NotNull String newName) throws Exception;

}
