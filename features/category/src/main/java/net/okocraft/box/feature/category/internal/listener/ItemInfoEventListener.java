package net.okocraft.box.feature.category.internal.listener;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.event.player.PlayerCollectItemInfoEvent;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

public class ItemInfoEventListener {

    private static BiFunction<Category, BoxItem, String> commandCreator;

    public static void setCommandCreator(@NotNull BiFunction<Category, BoxItem, String> commandCreator) {
        ItemInfoEventListener.commandCreator = Objects.requireNonNull(commandCreator);
    }

    private final CategoryRegistry registry;
    private final MessageKey.Arg1<Component> itemInfoFormat;
    private final MessageKey.Arg1<Component> categoryFormat;
    private final MessageKey categorySeparator;

    private final SubscribedListenerHolder listenerHolder = new SubscribedListenerHolder();

    public ItemInfoEventListener(@NotNull CategoryRegistry registry, @NotNull DefaultMessageCollector collector) {
        this.registry = registry;
        this.itemInfoFormat = MessageKey.arg1(collector.add("box.category.item-info.format", "<gray>Category: <aqua><categories>"), categories -> Argument.component("categories", categories));
        this.categoryFormat = MessageKey.arg1(collector.add("box.category.item-info.category-display", "<hover:show_text:'Click to open the menu'>[<category>]"), category -> Argument.component("category", category));
        this.categorySeparator = MessageKey.key(collector.add("box.category.item-info.category-separator", " "));
    }

    public void register(@NotNull Key listenerKey) {
        this.listenerHolder.subscribeAll(subscriber -> subscriber.add(PlayerCollectItemInfoEvent.class, listenerKey, this::processEvent));
    }

    public void unregister() {
        this.listenerHolder.unsubscribeAll();
    }

    private void processEvent(@NotNull PlayerCollectItemInfoEvent event) {
        event.addInfo(this.itemInfoFormat.apply(Component.join(
            JoinConfiguration.separator(this.categorySeparator),
            this.registry.values()
                .stream()
                .filter(category -> category.containsItem(event.getItem()))
                .map(category ->
                    this.categoryFormat.apply(category.getDisplayName(event.getBoxPlayer().getPlayer()))
                        .clickEvent(commandCreator != null ? ClickEvent.runCommand(commandCreator.apply(category, event.getItem())) : null))
                .toList()
        )));
    }
}
