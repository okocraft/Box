package net.okocraft.box.command.message;

import net.okocraft.box.api.message.argument.SingleArgument;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class CommandMessage {

    public static final SingleArgument<String> VERSION_INFO =
            version -> translatable()
                    .key("box.command.version.info")
                    .args(text(version, AQUA))
                    .color(GRAY)
                    .build();

    private CommandMessage() {
        throw new UnsupportedOperationException();
    }
}
