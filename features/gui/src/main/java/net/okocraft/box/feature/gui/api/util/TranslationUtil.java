package net.okocraft.box.feature.gui.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public final class TranslationUtil {

    public static @NotNull Component render(@NotNull Component component, @NotNull Player player) {
        return GlobalTranslator.render(component, player.locale());
    }

    public static @NotNull @Unmodifiable List<Component> render(@NotNull List<Component> list,
                                                                @NotNull Player player) {
        return list.stream().map(component -> render(component, player)).toList();
    }
}
