package net.okocraft.box.core.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ErrorMessages {

    public static final Component ERROR_LOAD_PLAYER_DATA_ON_JOIN =
            Component.translatable("box.error.player-data.load-on-join", NamedTextColor.RED);

    public static final Component ERROR_SAVE_PLAYER_DATA =
            Component.translatable("box.error.player-data.save", NamedTextColor.RED);

    public static final Component ERROR_COMMAND_NO_ARGUMENT =
            Component.translatable("box.error.command.no-argument", NamedTextColor.RED);

}
