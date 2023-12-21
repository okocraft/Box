package net.okocraft.box.feature.category;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.file.CategoryFile;
import net.okocraft.box.feature.category.internal.listener.CustomItemListener;
import net.okocraft.box.feature.category.internal.listener.ItemInfoEventListener;
import net.okocraft.box.feature.category.internal.registry.CategoryRegistryImpl;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CategoryFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private static final Key CUSTOM_ITEM_LISTENER_KEY = Key.key("box", "feature/category/custom_item_listener");
    private static final Key ITEM_INFO_COLLECT_EVENT_LISTENER_KEY = Key.key("box", "feature/category/item_info_collect_event");

    private final CategoryRegistry categoryRegistry = new CategoryRegistryImpl();
    private final CustomItemListener customItemListener = new CustomItemListener(categoryRegistry);
    private final ItemInfoEventListener itemInfoEventListener = new ItemInfoEventListener(categoryRegistry);

    public CategoryFeature() {
        super("category");
    }

    @Override
    public void enable() {
        var filepath = BoxAPI.api().getPluginDirectory().resolve("categories.yml");

        try {
            CategoryFile.load(this.categoryRegistry, filepath);
        } catch (IOException e) {
            BoxLogger.logger().error("Could not load categories.yml", e);
            return;
        }

        try {
            YamlFormat.DEFAULT.save(CategoryFile.dump(this.categoryRegistry), filepath);
        } catch (IOException e) {
            BoxLogger.logger().error("Could not save categories.yml", e);
            return;
        }

        this.customItemListener.register(CUSTOM_ITEM_LISTENER_KEY);
        this.itemInfoEventListener.register(ITEM_INFO_COLLECT_EVENT_LISTENER_KEY);
    }

    @Override
    public void disable() {
        this.customItemListener.unregister(CUSTOM_ITEM_LISTENER_KEY);
        this.itemInfoEventListener.unregister(ITEM_INFO_COLLECT_EVENT_LISTENER_KEY);
        this.categoryRegistry.unregisterAll();
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();
        sender.sendMessage(Components.grayTranslatable("box.category.reloaded"));
    }

    public @NotNull CategoryRegistry getCategoryRegistry() {
        return categoryRegistry;
    }
}
