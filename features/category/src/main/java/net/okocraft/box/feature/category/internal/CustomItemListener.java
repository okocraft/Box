package net.okocraft.box.feature.category.internal;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.item.CustomItemRegisterEvent;
import net.okocraft.box.api.event.item.CustomItemRenameEvent;
import net.okocraft.box.feature.category.CategoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class CustomItemListener {

    public void register(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getHandlerList(CustomItemRegisterEvent.class)
                .subscribe(listenerKey, this::processEvent);

        BoxProvider.get()
                .getEventBus()
                .getHandlerList(CustomItemRenameEvent.class)
                .subscribe(listenerKey, this::processEvent);
    }

    public void unregister(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getHandlerList(CustomItemRegisterEvent.class)
                .unsubscribeAll(listenerKey);

        BoxProvider.get()
                .getEventBus()
                .getHandlerList(CustomItemRenameEvent.class)
                .unsubscribeAll(listenerKey);
    }

    private void processEvent(@NotNull CustomItemRegisterEvent event) {
        for (var category : CategoryHolder.get()) {
            if (category instanceof BoxCategory boxCategory &&
                    category.getName().equals(DefaultCategory.CUSTOM_ITEMS.getName())) {
                boxCategory.add(event.getItem());
                return;
            }
        }
    }

    private void processEvent(@NotNull CustomItemRenameEvent event) {
        try (var yaml =
                     YamlConfiguration.create(BoxProvider.get().getPluginDirectory().resolve("categories.yml"))) {
            CategoryExporter.export(yaml);
            yaml.save();
        } catch (Exception e) {
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not load categories.yml", e);
        }
    }
}
