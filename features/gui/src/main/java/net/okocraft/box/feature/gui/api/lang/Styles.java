package net.okocraft.box.feature.gui.api.lang;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public final class Styles {

    private static final Style NO_DECORATION =
            Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build();

    public static final Style NO_DECORATION_GRAY = NO_DECORATION.color(GRAY);

    public static final Style NO_DECORATION_GOLD = NO_DECORATION.color(GOLD);

    public static final Style NO_DECORATION_AQUA = NO_DECORATION.color(AQUA);

    public static final Style NO_DECORATION_RED = NO_DECORATION.color(RED);

    public static final Style NO_DECORATION_YELLOW = NO_DECORATION.color(YELLOW);

    private Styles() {
        throw new UnsupportedOperationException();
    }
}
