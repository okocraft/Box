package net.okocraft.box.core.storage.model.data;

import com.github.siroshun09.configapi.api.Configuration;
import org.jetbrains.annotations.NotNull;

public interface CustomDataStorage {

    void init() throws Exception;

    void close() throws Exception;

    @NotNull Configuration load(@NotNull String namespace, @NotNull String key) throws Exception;

    void save(@NotNull String namespace, @NotNull String key, @NotNull Configuration configuration) throws Exception;
}
