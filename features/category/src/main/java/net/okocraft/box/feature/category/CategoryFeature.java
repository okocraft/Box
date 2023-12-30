package net.okocraft.box.feature.category;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.category.CustomItemCategory;
import net.okocraft.box.feature.category.internal.file.CategoryFile;
import net.okocraft.box.feature.category.internal.listener.CustomItemListener;
import net.okocraft.box.feature.category.internal.listener.ItemInfoEventListener;
import net.okocraft.box.feature.category.internal.registry.CategoryRegistryImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class CategoryFeature extends AbstractBoxFeature implements Reloadable {

    private static final Key CUSTOM_ITEM_LISTENER_KEY = Key.key("box", "feature/category/custom_item_listener");
    private static final Key ITEM_INFO_COLLECT_EVENT_LISTENER_KEY = Key.key("box", "feature/category/item_info_collect_event");

    private final Path filepath;
    private final CategoryRegistry categoryRegistry = new CategoryRegistryImpl();
    private final CustomItemCategory customItemCategory = new CustomItemCategory();
    private final CustomItemListener customItemListener;
    private final ItemInfoEventListener itemInfoEventListener;
    private final MiniMessageBase reloaded;

    public CategoryFeature(@NotNull FeatureContext.Registration context) {
        super("category");
        this.filepath = context.dataDirectory().resolve("categories.yml");
        this.customItemListener = new CustomItemListener(this.filepath, this.customItemCategory);
        this.itemInfoEventListener = new ItemInfoEventListener(this.categoryRegistry, context.defaultMessageCollector());
        CustomItemCategory.addDefaultCategoryName(context.defaultMessageCollector());
        this.reloaded = MiniMessageBase.messageKey(context.defaultMessageCollector().add("box.category.reloaded", "<gray>Categories have been reloaded."));
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) throws IOException {
        try (var file = new CategoryFile(this.filepath, this.categoryRegistry, BoxAPI.api().getItemManager())) {
            file.loadFile()
                    .convertIfUnknownVersion()
                    .readCategoriesIfExists()
                    .readCustomItemsIfExists(this.customItemCategory)
                    .addNewDefaultItemsIfNeeded();
        }

        this.categoryRegistry.register("custom-items", this.customItemCategory);
        this.customItemListener.register(CUSTOM_ITEM_LISTENER_KEY);
        this.itemInfoEventListener.register(ITEM_INFO_COLLECT_EVENT_LISTENER_KEY);
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        this.customItemListener.unregister(CUSTOM_ITEM_LISTENER_KEY);
        this.itemInfoEventListener.unregister(ITEM_INFO_COLLECT_EVENT_LISTENER_KEY);
        this.categoryRegistry.unregisterAll();
    }

    @Override
    public void reload(@NotNull FeatureContext.Reloading context) throws IOException {
        this.disable(context.asDisabling());
        this.enable(context.asEnabling());
        this.reloaded.source(BoxAPI.api().getMessageProvider().findSource(context.commandSender())).send(context.commandSender());
    }

    public @NotNull CategoryRegistry getCategoryRegistry() {
        return this.categoryRegistry;
    }
}
