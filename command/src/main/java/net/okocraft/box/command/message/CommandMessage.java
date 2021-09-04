package net.okocraft.box.command.message;

import net.okocraft.box.api.message.argument.QuadArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.model.item.BoxItem;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class CommandMessage {

    public static final SingleArgument<String> VERSION_INFO =
            version ->
                    translatable()
                            .key("box.command.version.info")
                            .args(text(version, AQUA))
                            .color(GRAY)
                            .build();

    public static final SingleArgument<String> GIVE_PLAYER_NOT_FOUND =
            playerName ->
                    translatable()
                            .key("box.command.boxadmin.give.player-not-found")
                            .args(text(playerName, AQUA))
                            .color(RED)
                            .build();

    public static final SingleArgument<String> GIVE_ITEM_NOT_FOUND =
            itemName ->
                    translatable()
                            .key("box.command.boxadmin.give.item-not-found")
                            .args(text(itemName, AQUA))
                            .color(RED)
                            .build();

    public static final SingleArgument<String> GIVE_INVALID_NUMBER =
            invalid ->
                    translatable()
                            .key("box.command.boxadmin.give.invalid-number")
                            .args(text(invalid, AQUA))
                            .color(RED)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_SENDER =
            (targetName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.give.success-sender")
                            .args(
                                    text(targetName, AQUA), item.getDisplayName(),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_TARGET =
            (senderName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.give.success-target")
                            .args(
                                    text(senderName, AQUA), item.getDisplayName(),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    private CommandMessage() {
        throw new UnsupportedOperationException();
    }
}
