package net.okocraft.box.api.message;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

import static net.okocraft.box.api.message.Placeholders.ARG;
import static net.okocraft.box.api.message.Placeholders.ITEM_NAME;
import static net.okocraft.box.api.message.Placeholders.PERMISSION;
import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

/**
 * A class that holds common error messages.
 */
public final class ErrorMessages {

    /**
     * A message sent when the player does not have the permission.
     */
    public static final MessageKey.Arg1<String> NO_PERMISSION = getKeyFromCore("NO_PERMISSION").with(PERMISSION);

    /**
     * A message sent when the player cannot use Box.
     */
    public static final MessageKey CANNOT_USE_BOX = getKeyFromCore("CANNOT_USE_BOX");

    /**
     * A message sent when the command executed by the non-player.
     */
    public static final MessageKey COMMAND_ONLY_PLAYER = getKeyFromCore("ONLY_PLAYER");

    /**
     * A message sent when the specified argument is not number.
     */
    public static final MessageKey.Arg1<String> INVALID_NUMBER = getKeyFromCore("INVALID_NUMBER").with(ARG);

    /**
     * A message sent when the specified item is not found.
     */
    public static final MessageKey.Arg1<String> ITEM_NOT_FOUND = getKeyFromCore("ITEM_NOT_FOUND").with(ITEM_NAME);

    /**
     * A message sent when the specified player is not found.
     */
    public static final MessageKey.Arg1<String> PLAYER_NOT_FOUND = getKeyFromCore("PLAYER_NOT_FOUND").with(PLAYER_NAME);

    /**
     * A message sent when the specified subcommand is not found.
     */
    public static final MessageKey SUB_COMMAND_NOT_FOUND = getKeyFromCore("SUB_COMMAND_NOT_FOUND");

    /**
     * A message sent when arguments are not enough.
     */
    public static final MessageKey NOT_ENOUGH_ARGUMENT = getKeyFromCore("NOT_ENOUGH_ARGUMENT");

    private static final MessageKey NOT_LOADED_SELF = getKeyFromCore("NOT_LOADED_SELF");
    private static final MessageKey.Arg1<String> NOT_LOADED_OTHER = getKeyFromCore("NOT_LOADED_OTHER").with(PLAYER_NAME);
    private static final MessageKey LOADING_SELF = getKeyFromCore("LOADING_SELF");
    private static final MessageKey.Arg1<String> LOADING_OTHER = getKeyFromCore("LOADING_OTHER").with(PLAYER_NAME);

    private static @NotNull MessageKey getKeyFromCore(@NotNull String fieldName) {
        try {
            Class<?> clazz = Class.forName("net.okocraft.box.core.message.CoreMessages");
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return MessageKey.key((String) field.get(null));
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a message sent when the player data is not loaded.
     *
     * @param playerName a player name or {@code null} if self
     * @return a message sent when the player data is not loaded
     */
    public static @NotNull ComponentLike playerDataIsNotLoaded(@Nullable String playerName) {
        return playerName == null ? NOT_LOADED_SELF : NOT_LOADED_OTHER.apply(playerName);
    }

    /**
     * Creates a message sent when the player data is currently loading.
     *
     * @param playerName a player name or {@code null} if self
     * @return a message sent when the player data is currently loading
     */
    public static @NotNull ComponentLike playerDataIsLoading(@Nullable String playerName) {
        return playerName == null ? LOADING_SELF : LOADING_OTHER.apply(playerName);
    }

    private ErrorMessages() {
        throw new UnsupportedOperationException();
    }
}
