package net.okocraft.box.feature.category.impl;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.item.CustomItemRegisterEvent;
import net.okocraft.box.feature.category.CategoryHolder;
import org.jetbrains.annotations.NotNull;

public class CustomItemListener {

    public void register(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getHandlerList(CustomItemRegisterEvent.class)
                .subscribe(listenerKey, this::processEvent);
    }

    public void unregister(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getHandlerList(CustomItemRegisterEvent.class)
                .unsubscribeAll(listenerKey);
    }

    private void processEvent(@NotNull CustomItemRegisterEvent event) {
        for (var category : CategoryHolder.get()) {
            if (category instanceof BoxCategory boxCategory &&
                    category.getName().equals(DefaultCategory.CUSTOM_ITEMS.getName())) {
                boxCategory.add(event.getNewItem());
            }
        }
    }
}
