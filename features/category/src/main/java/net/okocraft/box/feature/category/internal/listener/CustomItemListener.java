package net.okocraft.box.feature.category.internal.listener;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import com.github.siroshun09.event4j.key.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.item.CustomItemRegisterEvent;
import net.okocraft.box.api.event.item.CustomItemRenameEvent;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.category.CommonDefaultCategory;
import net.okocraft.box.feature.category.internal.file.CategoryFile;
import org.jetbrains.annotations.NotNull;

public class CustomItemListener {

    private final CategoryRegistry registry;

    public CustomItemListener(@NotNull CategoryRegistry registry) {
        this.registry = registry;
    }

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
        var category = registry.getByName(CommonDefaultCategory.CUSTOM_ITEMS.getName()).orElseThrow(() -> new IllegalStateException("Where is the custom category!?"));
        category.addItem(event.getItem());
        updateCategoriesFile();
    }

    private void processEvent(@NotNull CustomItemRenameEvent event) {
        updateCategoriesFile();
    }

    private void updateCategoriesFile() {
        try {
            var filepath = BoxProvider.get().getPluginDirectory().resolve("categories.yml");
            YamlFormat.DEFAULT.save(CategoryFile.dump(this.registry), filepath);
        } catch (Exception e) {
            BoxLogger.logger().error("Could not save categories.yml", e);
        }
    }
}
