package net.okocraft.box.feature.category;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.category.impl.CategoryLoader;
import net.okocraft.box.feature.category.impl.CustomItemListener;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class CategoryFeature extends AbstractBoxFeature implements Reloadable {

    private final CustomItemListener customItemListener = new CustomItemListener();

    public CategoryFeature() {
        super("category");
    }

    @Override
    public void enable() {
        try (var yaml =
                     YamlConfiguration.create(BoxProvider.get().getPluginDirectory().resolve("categories.yml"))) {
            ResourceUtils.copyFromJar(BoxProvider.get().getJar(), "categories.yml", yaml.getPath());
            yaml.load();
            CategoryHolder.addAll(CategoryLoader.load(yaml).export(yaml).categoryList());
        } catch (Exception e) {
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not load categories.yml", e);
        }

        customItemListener.register(getListenerKey());
    }

    @Override
    public void disable() {
        customItemListener.unregister(getListenerKey());
        CategoryHolder.get().clear();
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();
        sender.sendMessage(Component.translatable("box.category.reloaded", NamedTextColor.GRAY));
    }
}
