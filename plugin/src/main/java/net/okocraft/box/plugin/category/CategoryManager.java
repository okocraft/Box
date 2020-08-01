package net.okocraft.box.plugin.category;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.config.CategoryConfig;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CategoryManager {

    private final Box plugin;
    private final List<Category> categories = new LinkedList<>();

    public CategoryManager(@NotNull Box plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!categories.isEmpty()) {
            categories.clear();
        }

        CategoryConfig config = new CategoryConfig(plugin);

        if (!config.isLoaded()) {
            throw new IllegalStateException("Could not load category.yml");
        }

        for (String category : config.getCategories()) {
            String displayName = config.getDisplayName(category);

            ItemStack icon =
                    new ItemBuilder()
                            .setMaterial(config.getIconMaterial(category))
                            .setDisplayName(displayName)
                            .build();

            List<Item> items = config.getItems(category);

            categories.add(new Category(category, displayName, icon, items));
        }
    }

    @NotNull
    @Unmodifiable
    public List<Category> getCategories() {
        return List.copyOf(categories);
    }

    @NotNull
    public Optional<Category> getCategory(@NotNull String name) {
        return categories.stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    @NotNull
    public Optional<Category> getCategory(@NotNull Item item) {
        return categories.stream().filter(c -> c.getItems().contains(item)).findFirst();
    }

    public void addCategory(@NotNull String name, @NotNull Material material, @NotNull List<Item> items) {
        ItemStack icon = new ItemBuilder().setMaterial(material).setDisplayName(name).build();

        Category category = new Category(name, name, icon, items);

        categories.add(category);
    }

    public void save() {
        CategoryConfig config = new CategoryConfig(plugin);

        if (!config.isLoaded()) {
            throw new IllegalStateException("Could not load category.yml");
        }

        List.copyOf(categories).forEach(config::setCategory);

        if (!config.save()) {
            throw new IllegalStateException("Could not save category.yml");
        }
    }
}
