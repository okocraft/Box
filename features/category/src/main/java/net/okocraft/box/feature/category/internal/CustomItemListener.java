package net.okocraft.box.feature.category.internal;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.event4j.key.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.item.CustomItemRegisterEvent;
import net.okocraft.box.api.event.item.CustomItemRenameEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.category.CategoryHolder;
import net.okocraft.box.feature.category.model.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.logging.Level;

public class CustomItemListener {

    public void register(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getSubscriber(CustomItemRegisterEvent.class)
                .subscribe(listenerKey, this::processEvent);

        BoxProvider.get()
                .getEventBus()
                .getSubscriber(CustomItemRenameEvent.class)
                .subscribe(listenerKey, this::processEvent);
    }

    public void unregister(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getSubscriber(CustomItemRegisterEvent.class)
                .unsubscribeAll(listenerKey);

        BoxProvider.get()
                .getEventBus()
                .getSubscriber(CustomItemRenameEvent.class)
                .unsubscribeAll(listenerKey);
    }

    private void processEvent(@NotNull CustomItemRegisterEvent event) {
        for (var category : CategoryHolder.get()) {
            if (category instanceof BoxCategory boxCategory &&
                    category.getName().equals(DefaultCategory.CUSTOM_ITEMS.getName())) {
                boxCategory.add(event.getItem());
                updateCategoriesFile(category, event.getItem(), null);
                return;
            }
        }
    }

    private void processEvent(@NotNull CustomItemRenameEvent event) {
        for (var category : CategoryHolder.get()) {
            if (category.getItems().contains(event.getItem())) {
                updateCategoriesFile(category, event.getItem(), event.getPreviousName());
                return;
            }
        }
    }

    private void updateCategoriesFile(@NotNull Category category, @NotNull BoxItem item, @Nullable String oldName) {
        try (var yaml =
                     YamlConfiguration.create(BoxProvider.get().getPluginDirectory().resolve("categories.yml"))) {
            yaml.load();

            var itemNameList = new ArrayList<>(yaml.getStringList(category.getName()));

            if (oldName == null) {
                itemNameList.add(item.getPlainName());
            } else {
                var index = itemNameList.indexOf(oldName);

                if (index != -1) {
                    itemNameList.remove(index);
                    itemNameList.add(index, item.getPlainName());
                } else {
                    itemNameList.add(item.getPlainName());
                }
            }

            yaml.set(category.getName(), itemNameList);

            yaml.save();
        } catch (Exception e) {
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not save categories.yml", e);
        }
    }
}
