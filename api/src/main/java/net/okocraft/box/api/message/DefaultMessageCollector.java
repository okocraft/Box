package net.okocraft.box.api.message;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that collects default messages.
 */
public interface DefaultMessageCollector {

    /**
     * Adds a default message with the key.
     *
     * @param key            a key of message
     * @param defaultMessage a default message
     * @return the given key
     */
    @Contract("_, _ -> param1")
    @NotNull String add(@NotNull String key, @NotNull String defaultMessage);

}
