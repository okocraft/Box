package net.okocraft.box.core.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.SingleArgument;

import static net.okocraft.box.api.message.Components.aquaText;
import static net.okocraft.box.api.message.Components.redTranslatable;
import static net.okocraft.box.api.message.Components.whiteText;

public final class ErrorMessages {

    public static final Component ERROR_LOAD_PLAYER_DATA_ON_JOIN =
            redTranslatable("box.error.player-data.load-on-join");

    public static final Component ERROR_SAVE_PLAYER_DATA =
            redTranslatable("box.error.player-data.save");

    public static final Component ERROR_COMMAND_NO_ARGUMENT =
            redTranslatable("box.error.command.no-argument");

    public static final DoubleArgument<String, Throwable> ERROR_RELOAD_FAILURE =
            (name, throwable) ->
                    redTranslatable(
                            "box.error.failed-to-reload-feature",
                            aquaText(name), whiteText(throwable.getMessage())
                    );

    public static final SingleArgument<Throwable> ERROR_WHILE_EXECUTING_COMMAND =
            throwable ->
                    redTranslatable(
                            "box.error.failed-to-execute-command",
                            whiteText(throwable.getMessage())
                    );
}
