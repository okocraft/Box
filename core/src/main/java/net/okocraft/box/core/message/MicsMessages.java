package net.okocraft.box.core.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class MicsMessages {

    public static final Component CONFIG_RELOADED =
            Component.translatable("box.mics.config-reloaded", NamedTextColor.GRAY);

    public static final Component LANGUAGES_RELOADED =
            Component.translatable("box.mics.languages-reloaded", NamedTextColor.GRAY);

    private MicsMessages() {
        throw new UnsupportedOperationException();
    }
}
