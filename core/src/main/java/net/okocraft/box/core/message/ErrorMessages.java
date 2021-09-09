package net.okocraft.box.core.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.SingleArgument;

public final class ErrorMessages {

    public static final Component ERROR_LOAD_PLAYER_DATA_ON_JOIN =
            Component.translatable("box.error.player-data.load-on-join", NamedTextColor.RED);

    public static final Component ERROR_SAVE_PLAYER_DATA =
            Component.translatable("box.error.player-data.save", NamedTextColor.RED);

    public static final Component ERROR_COMMAND_NO_ARGUMENT =
            Component.translatable("box.error.command.no-argument", NamedTextColor.RED);

    public static final DoubleArgument<String, Throwable> ERROR_RELOAD_FAILURE =
            (name, throwable) ->
                    Component.translatable()
                            .key("box.error.reload.failure")
                            .args(
                                    Component.text(name, NamedTextColor.AQUA),
                                    Component.text(throwable.getMessage(), NamedTextColor.WHITE)
                            )
                            .color(NamedTextColor.RED)
                            .build();

    public static final SingleArgument<Throwable> ERROR_WHILE_EXECUTING_COMMAND =
            throwable ->
                    Component.translatable()
                            .key("box.error.failed-to-execute-command")
                            .args(Component.text(throwable.getMessage(), NamedTextColor.WHITE))
                            .color(NamedTextColor.RED)
                            .build();
}
