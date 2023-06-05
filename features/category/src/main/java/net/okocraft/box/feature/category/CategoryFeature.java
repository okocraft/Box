package net.okocraft.box.feature.category;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.file.BundledCategoryFile;
import net.okocraft.box.feature.category.internal.file.CategoryDumper;
import net.okocraft.box.feature.category.internal.file.CategoryLoader;
import net.okocraft.box.feature.category.internal.listener.CustomItemListener;
import net.okocraft.box.feature.category.internal.listener.ItemInfoEventListener;
import net.okocraft.box.feature.category.internal.registry.CategoryRegistryImpl;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.util.logging.Level;

public class CategoryFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private final CategoryRegistry categoryRegistry = new CategoryRegistryImpl();
    private final CustomItemListener customItemListener = new CustomItemListener(categoryRegistry);
    private final ItemInfoEventListener itemInfoEventListener = new ItemInfoEventListener(categoryRegistry);

    public CategoryFeature() {
        super("category");
    }

    @Override
    public void enable() {
        try (var yaml = YamlConfiguration.create(BoxProvider.get().getPluginDirectory().resolve("categories.yml"))) {
            if (!Files.exists(yaml.getPath())) {
                BundledCategoryFile.copy(yaml.getPath());
            }

            yaml.load();

            CategoryLoader.load(categoryRegistry, yaml);

            yaml.clear();

            CategoryDumper.dump(categoryRegistry, yaml);

            yaml.save();
        } catch (Exception e) {
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not load categories.yml", e);
        }

        customItemListener.register(getListenerKey());
        itemInfoEventListener.register(getListenerKey());
    }

    @Override
    public void disable() {
        customItemListener.unregister(getListenerKey());
        itemInfoEventListener.unregister(getListenerKey());
        categoryRegistry.unregisterAll();
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
