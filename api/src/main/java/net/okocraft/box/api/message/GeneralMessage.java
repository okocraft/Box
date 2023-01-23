package net.okocraft.box.api.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static net.okocraft.box.api.message.Components.aquaText;
import static net.okocraft.box.api.message.Components.redTranslatable;
import static net.okocraft.box.api.message.Components.whiteTranslatable;

/**
 * A class that holds general messages
 */
public final class GeneralMessage {

    /**
     * A message sent when an executor don't have a permission.
     */
    public static final SingleArgument<String> ERROR_NO_PERMISSION =
            node -> redTranslatable("box.error.no-permission", aquaText(node));

    /**
     * A message sent when an executor of the command is not a player.
     */
    public static final Component ERROR_COMMAND_ONLY_PLAYER =
            redTranslatable("box.error.command.only-player");

    /**
     * A message sent when the subcommand not found.
     */
    public static final Component ERROR_COMMAND_SUBCOMMAND_NOT_FOUND =
            redTranslatable("box.error.command.subcommand-not-found");

    /**
     * A message sent when arguments are not enough.
     */
    public static final Component ERROR_COMMAND_NOT_ENOUGH_ARGUMENT =
            redTranslatable("box.error.command.not-enough-argument");

    /**
     * A message sent when the specified player was not found.
     */
    public static final SingleArgument<String> ERROR_COMMAND_PLAYER_NOT_FOUND =
            playerName -> redTranslatable("box.error.command.player-not-found", aquaText(playerName));

    /**
     * A message sent when the specified item was not found.
     */
    public static final SingleArgument<String> ERROR_COMMAND_ITEM_NOT_FOUND =
            itemName -> redTranslatable("box.error.command.item-not-found", aquaText(itemName));

    /**
     * A message sent when the specified number is invalid.
     */
    public static final SingleArgument<String> ERROR_COMMAND_INVALID_NUMBER =
            invalid -> redTranslatable("box.error.command.invalid-number", aquaText(invalid));

    /**
     * A message sent when Box is disabled in that world.
     */
    public static final SingleArgument<World> ERROR_DISABLED_WORLD =
            world -> redTranslatable("box.error.disabled-world", aquaText(world.getName()));

    /**
     * A message sent when the player is not loaded.
     */
    public static final Component ERROR_PLAYER_NOT_LOADED =
            redTranslatable("box.error.player-data.not-loaded.self");

    /**
     * A message sent when the targeted player is not loaded.
     */
    public static final SingleArgument<Player> ERROR_TARGET_PLAYER_NOT_LOADED =
            target -> redTranslatable("box.error.player-data.not-loaded.other", aquaText(target.getName()));

    /**
     * A message sent when the player is currently loading.
     */
    public static final Component ERROR_PLAYER_LOADING = redTranslatable("box.error.player-data.loading.self");

    /**
     * A message sent when the targeted player is currently loading.
     */
    public static final SingleArgument<Player> ERROR_TARGET_PLAYER_LOADING =
            target -> redTranslatable("box.error.player-data.loading.other", aquaText(target.getName()));

    /**
     * A component to use to hover text (click to copy).
     */
    public static final Component HOVER_TEXT_CLICK_TO_COPY = whiteTranslatable("box.mics.hover-text.click-to-copy");
}
