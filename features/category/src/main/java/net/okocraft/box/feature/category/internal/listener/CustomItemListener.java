package net.okocraft.box.feature.category.internal.listener;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
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
        BoxAPI.api().getEventManager().getSubscriber(CustomItemRegisterEvent.class).subscribe(listenerKey, this::processEvent);
        BoxAPI.api().getEventManager().getSubscriber(CustomItemRenameEvent.class).subscribe(listenerKey, this::processEvent);
    }

    public void unregister(@NotNull Key listenerKey) {
        BoxAPI.api().getEventManager().getSubscriber(CustomItemRegisterEvent.class).unsubscribeByKey(listenerKey);
        BoxAPI.api().getEventManager().getSubscriber(CustomItemRenameEvent.class).unsubscribeByKey(listenerKey);
    }

    private void processEvent(@NotNull CustomItemRegisterEvent event) {
        var category = registry.getByName(CommonDefaultCategory.CUSTOM_ITEMS.getName()).orElseThrow(() -> new IllegalStateException("Where is the custom category!?"));
        category.addItem(event.getItem());
        updateCategoriesFile();
    }

    private void processEvent(@NotNull CustomItemRenameEvent ignored) {
        updateCategoriesFile();
    }

    private void updateCategoriesFile() {
        try {
            var filepath = BoxAPI.api().getPluginDirectory().resolve("categories.yml");
            YamlFormat.DEFAULT.save(CategoryFile.dump(this.registry), filepath);
        } catch (Exception e) {
            BoxLogger.logger().error("Could not save categories.yml", e);
        }
    }
}
