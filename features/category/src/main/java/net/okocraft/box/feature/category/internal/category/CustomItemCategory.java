package net.okocraft.box.feature.category.internal.category;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CustomItemCategory extends AbstractCategory {

    public static final String KEY = "$custom-items";
    private static final String DISPLAY_NAME_KEY = "box.category.name.custom-items";

    public static void addDefaultCategoryName(@NotNull DefaultMessageCollector collector) {
        collector.add(DISPLAY_NAME_KEY, "Custom Items");
    }

    private final MiniMessageBase displayName = MiniMessageBase.messageKey(DISPLAY_NAME_KEY);

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.NETHER_STAR;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull Player viewer) {
        return this.displayName.create(BoxAPI.api().getMessageProvider().findSource(viewer));
    }

}
