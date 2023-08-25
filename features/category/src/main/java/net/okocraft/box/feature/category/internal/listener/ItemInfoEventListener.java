package net.okocraft.box.feature.category.internal.listener;

import com.github.siroshun09.event4j.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.player.PlayerCollectItemInfoEvent;
import net.okocraft.box.api.model.item.BoxItem;
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

    private static final HoverEvent<Component> CLICK_TO_OPEN_MENU =
            HoverEvent.showText(Component.translatable("box.category.item-info.click-to-open"));

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
                .map(category -> formatCategory(category, event.getItem()))
                .forEach(info::append);

        event.addInfo(info.build());
    }

    private @NotNull Component formatCategory(@NotNull Category category, @NotNull BoxItem item) {
        return Component.space().append(
                Component.text()
                        .append(Component.text("["))
                        .append(category.getDisplayName())
                        .append(Component.text("]"))
                        .color(NamedTextColor.AQUA)
                        .hoverEvent(CLICK_TO_OPEN_MENU)
                        .clickEvent(ClickEvent.runCommand(createCommand(category, item)))
        );
    }

    private @NotNull String createCommand(@NotNull Category category, @NotNull BoxItem item) {
        int page = category.getItems().indexOf(item) / 45 + 1;
        return "/box gui --category " + registry.getRegisteredName(category) + " --page " + page;
    }
}
