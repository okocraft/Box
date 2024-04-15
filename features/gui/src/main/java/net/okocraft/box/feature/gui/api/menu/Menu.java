package net.okocraft.box.feature.gui.api.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Menu {

    default void onOpen(@NotNull PlayerSession session) {
    }

    int getRows();

    @NotNull Component getTitle(@NotNull PlayerSession session);

    @NotNull List<? extends Button> getButtons(@NotNull PlayerSession session);
}
