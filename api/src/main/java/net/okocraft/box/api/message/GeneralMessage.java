package net.okocraft.box.api.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

/**
 * A class that holds general messages
 */
public final class GeneralMessage {

    /**
     * A message sent when an executor don't have a permission.
     */
    public static final SingleArgument<String> ERROR_NO_PERMISSION =
            node -> translatable("box.error.no-permission", RED).args(text(node, AQUA));

    /**
     * A message sent when an executor of the command is not a player.
     */
    public static final Component ERROR_COMMAND_ONLY_PLAYER = translatable("box.error.command.only-player", RED);

    /**
     * A message sent when the subcommand not found.
     */
    public static final Component ERROR_COMMAND_SUBCOMMAND_NOT_FOUND =
            translatable("box.error.command.subcommand-not-found", RED);

    /**
     * A message sent when arguments are not enough.
     */
    public static final Component ERROR_COMMAND_NOT_ENOUGH_ARGUMENT =
            translatable("box.error.command.not-enough-argument", RED);
}
