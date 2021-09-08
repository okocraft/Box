package net.okocraft.box.feature.category;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.category.impl.CategoryLoader;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class CategoryFeature extends AbstractBoxFeature implements Reloadable {

    public CategoryFeature() {
        super("category");
    }

    @Override
    public void enable() {
        try (var yaml =
                     YamlConfiguration.create(BoxProvider.get().getPluginDirectory().resolve("categories.yml"))) {
            yaml.load();
            CategoryHolder.addAll(CategoryLoader.load(yaml).export(yaml).categoryList());
        } catch (Exception e) {
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not load categories.yml", e);
        }
    }

    @Override
    public void disable() {
        CategoryHolder.get().clear();
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();
        sender.sendMessage(Component.translatable("box.category.reloaded", NamedTextColor.GRAY));
    }
}
