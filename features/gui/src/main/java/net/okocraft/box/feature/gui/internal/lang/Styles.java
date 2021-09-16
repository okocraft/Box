package net.okocraft.box.feature.gui.internal.lang;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

public final class Styles {

    public static final Style NO_STYLE =
            Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build();

    private Styles() {
        throw new UnsupportedOperationException();
    }
}
