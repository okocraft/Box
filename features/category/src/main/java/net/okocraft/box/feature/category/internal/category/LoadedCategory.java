package net.okocraft.box.feature.category.internal.category;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

public final class LoadedCategory extends AbstractCategory {

    private final Material icon;
    private final Map<Locale, String> displayNameMap;

    public LoadedCategory(@NotNull Material icon, @NotNull Map<Locale, String> displayNameMap) {
        this.icon = icon;
        this.displayNameMap = displayNameMap;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return this.icon;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull Player viewer) {
        String result = this.displayNameMap.get(viewer.locale());
        if (result == null) result = this.displayNameMap.get(Locale.of(viewer.locale().getLanguage()));
        return Component.text(result != null ? result : this.displayNameMap.getOrDefault(null, "Unknown"));
    }

}
