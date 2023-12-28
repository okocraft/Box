package net.okocraft.box.api.message;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
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
    public static final Arg1<String> NO_PERMISSION = arg1(getKeyFromCore("NO_PERMISSION"), PERMISSION);

    /**
     * A message sent when the player cannot use Box.
     */
    public static final MiniMessageBase CANNOT_USE_BOX = messageKey(getKeyFromCore("CANNOT_USE_BOX"));

    /**
     * A message sent when the command executed by the non-player.
     */
    public static final MiniMessageBase COMMAND_ONLY_PLAYER = messageKey(getKeyFromCore("ONLY_PLAYER"));

    /**
     * A message sent when the specified argument is not number.
     */
    public static final Arg1<String> INVALID_NUMBER = arg1(getKeyFromCore("INVALID_NUMBER"), ARG);

    /**
     * A message sent when the specified item is not found.
     */
    public static final Arg1<String> ITEM_NOT_FOUND = arg1(getKeyFromCore("ITEM_NOT_FOUND"), ITEM_NAME);

    /**
     * A message sent when the specified player is not found.
     */
    public static final Arg1<String> PLAYER_NOT_FOUND = arg1(getKeyFromCore("PLAYER_NOT_FOUND"), PLAYER_NAME);

    /**
     * A message sent when the specified subcommand is not found.
     */
    public static final MiniMessageBase SUB_COMMAND_NOT_FOUND = messageKey(getKeyFromCore("SUB_COMMAND_NOT_FOUND"));

    /**
     * A message sent when arguments are not enough.
     */
    public static final MiniMessageBase NOT_ENOUGH_ARGUMENT = messageKey(getKeyFromCore("NOT_ENOUGH_ARGUMENT"));

    private static final MiniMessageBase NOT_LOADED_SELF = messageKey(getKeyFromCore("NOT_LOADED_SELF"));
    private static final Arg1<String> NOT_LOADED_OTHER = arg1(getKeyFromCore("NOT_LOADED_OTHER"), PLAYER_NAME);
    private static final MiniMessageBase LOADING_SELF = messageKey(getKeyFromCore("LOADING_SELF"));
    private static final Arg1<String> LOADING_OTHER = arg1(getKeyFromCore("LOADING_OTHER"), PLAYER_NAME);

    private static @NotNull String getKeyFromCore(@NotNull String fieldName) {
        try {
            var clazz = Class.forName("net.okocraft.box.core.message.CoreMessages");
            var field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(null);
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
    public static @NotNull MiniMessageBase playerDataIsNotLoaded(@Nullable String playerName) {
        return playerName == null ? NOT_LOADED_SELF : NOT_LOADED_OTHER.apply(playerName);
    }

    /**
     * Creates a message sent when the player data is currently loading.
     *
     * @param playerName a player name or {@code null} if self
     * @return a message sent when the player data is currently loading
     */
    public static @NotNull MiniMessageBase playerDataIsLoading(@Nullable String playerName) {
        return playerName == null ? LOADING_SELF : LOADING_OTHER.apply(playerName);
    }

    private ErrorMessages() {
        throw new UnsupportedOperationException();
    }
}
