package net.okocraft.box.storage.api.model.data;

import com.github.siroshun09.configapi.api.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface CustomDataStorage {

    void init() throws Exception;

    @NotNull Configuration load(@NotNull String namespace, @NotNull String key) throws Exception;

    void save(@NotNull String namespace, @NotNull String key, @NotNull Configuration configuration) throws Exception;

    @NotNull Collection<Key> getKeys() throws Exception;

    // FIXME: improve this
    record Key(@NotNull String namespace, @NotNull String key) {
    }
}
