package net.okocraft.box.feature.category.internal.listener;

import com.github.siroshun09.event4j.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.player.PlayerCollectItemInfoEvent;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import org.jetbrains.annotations.NotNull;

public class ItemInfoEventListener {

    private static final Component ITEM_INFO_PREFIX =
            Component.text()
                    .append(Component.translatable("box.category.item-info.prefix"))
                    .append(Component.text(":"))
                    .color(NamedTextColor.GRAY)
                    .build();

    private final CategoryRegistry registry;

    public ItemInfoEventListener(@NotNull CategoryRegistry registry) {
        this.registry = registry;
    }

    public void register(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getSubscriber(PlayerCollectItemInfoEvent.class)
                .subscribe(listenerKey, this::processEvent);
    }

    public void unregister(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getSubscriber(PlayerCollectItemInfoEvent.class)
                .unsubscribeAll(listenerKey);
    }

    private void processEvent(@NotNull PlayerCollectItemInfoEvent event) {
        var info = Component.text().append(ITEM_INFO_PREFIX);

        registry.values().stream()
                .filter(category -> category.containsItem(event.getItem()))
                .map(this::formatCategory)
                .forEach(info::append);

        event.addInfo(info.build());
    }

    private @NotNull Component formatCategory(@NotNull Category category) {
        var displayName =
                Component.text()
                        .append(Component.text("["))
                        .append(category.getDisplayName())
                        .append(Component.text("]"))
                        .color(NamedTextColor.AQUA);

        return Component.space().append(displayName);
    }
}
