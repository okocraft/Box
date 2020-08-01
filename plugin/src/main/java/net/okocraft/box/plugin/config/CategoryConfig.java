package net.okocraft.box.plugin.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.category.Category;
import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CategoryConfig extends BukkitConfig {

    private final Box plugin;

    public CategoryConfig(@NotNull Box plugin) {
        super(plugin, "category.yml", true);

        this.plugin = plugin;
    }

    @NotNull
    public List<String> getCategories() {
        return List.copyOf(getKeys());
    }

    @NotNull
    public String getDisplayName(@NotNull String category) {
        return getString(category + ".display-name", category);
    }

    @NotNull
    public Material getIconMaterial(@NotNull String category) {
        String path = category + ".icon";
        String value = getString(path);

        try {
            return Material.valueOf(value);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material name: " + value + " (" + path + ")");
            return Material.BARRIER;
        }
    }

    @NotNull
    public List<Item> getItems(@NotNull String category) {
        List<Item> result = new LinkedList<>();

        for (String itemName : config.getStringList(category + ".items")) {
            Optional<Item> item = plugin.getItemManager().getItemByName(itemName);

            if (item.isPresent()) {
                result.add(item.get());
            } else {
                plugin.getLogger().warning("Could not get item: " + itemName + " (category: " + category + ")");
            }
        }

        return result;
    }

    public void setCategory(@NotNull Category category) {
        set(category.getName() + ".display-name", category.getDisplayName());
        set(category.getName() + ".icon", category.getIcon().getType().toString());
        set(category.getName() + ".items", category.getItems().stream().map(Item::getName).collect(Collectors.toList()));
    }
}
