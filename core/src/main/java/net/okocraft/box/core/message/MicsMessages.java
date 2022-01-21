package net.okocraft.box.core.message;

import net.kyori.adventure.text.Component;

import static net.okocraft.box.api.message.Components.grayTranslatable;

public final class MicsMessages {

    public static final Component CONFIG_RELOADED = grayTranslatable("box.mics.config-reloaded");

    public static final Component LANGUAGES_RELOADED = grayTranslatable("box.mics.languages-reloaded");

    private MicsMessages() {
        throw new UnsupportedOperationException();
    }
}
