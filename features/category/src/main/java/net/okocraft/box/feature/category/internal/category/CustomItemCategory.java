package net.okocraft.box.feature.category.internal.category;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class CustomItemCategory extends AbstractCategory {

    public static final String CONFIG_KEY = "$custom-items";
    public static final String REGISTRY_KEY = "custom-items";
    private static final String DISPLAY_NAME_KEY = "box.category.name.custom-items";

    public static void addDefaultCategoryName(@NotNull DefaultMessageCollector collector) {
        collector.add(DISPLAY_NAME_KEY, "Custom Items");
    }

    private final CategoryRegistry registry;
    private final MiniMessageBase displayName = MiniMessageBase.messageKey(DISPLAY_NAME_KEY);

    public CustomItemCategory(CategoryRegistry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.NETHER_STAR;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull Player viewer) {
        return this.displayName.create(BoxAPI.api().getMessageProvider().findSource(viewer));
    }

    @Override
    public void addItem(@NotNull BoxItem item) {
        super.addItem(item);
        if (this.getItems().size() == 1) {
            this.registry.register(REGISTRY_KEY, this);
        }
    }

    @Override
    public void addItems(@NotNull Collection<BoxItem> items) {
        super.addItems(items);
        if (!items.isEmpty() && this.getItems().size() == items.size()) {
            this.registry.register(REGISTRY_KEY, this);
        }
    }

    @Override
    public void removeItem(@NotNull BoxItem item) {
        super.removeItem(item);
        if (this.getItems().isEmpty()) {
            this.registry.unregister(REGISTRY_KEY);
        }
    }
}
