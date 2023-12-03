package net.okocraft.box.feature.category.internal.file;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.categorizer.ExperimentalItems;
import net.okocraft.box.feature.category.internal.category.CommonDefaultCategory;
import net.okocraft.box.feature.category.internal.category.DefaultCategory;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class CategoryFile {

    public static void load(@NotNull CategoryRegistry registry, @NotNull Path filepath) throws IOException {
        var loaded = Files.isRegularFile(filepath) ? YamlFormat.DEFAULT.load(filepath) : MapNode.empty();
        var defaultCategoryFile = BundledCategoryFile.loadDefaultCategoryFile();

        CategoryFile.load(registry, loaded, defaultCategoryFile);
    }

    private static void load(@NotNull CategoryRegistry registry, @NotNull MapNode source, @NotNull MapNode defaultCategoryFile) {
        var itemManager = BoxProvider.get().getItemManager();
        var uncategorizedItems = new ObjectOpenHashSet<>(itemManager.getItemList());
        var iconMap = source.getMap("icons");

        for (var entry : source.value().entrySet()) {
            var key = entry.getKey();

            if (key.equals("icons") ||
                    key.equals(CommonDefaultCategory.UNCATEGORIZED.getName()) ||
                    key.equals(CommonDefaultCategory.CUSTOM_ITEMS.getName())) {
                continue;
            }

            Material iconMaterial;

            var iconMaterialName = iconMap.getString(key).toUpperCase(Locale.ROOT);

            try {
                iconMaterial = Material.valueOf(iconMaterialName);
            } catch (IllegalArgumentException e) {
                BoxProvider.get().getLogger().warning("Unknown icon: " + iconMaterialName + " (icons." + key + ")");
                iconMaterial = Material.STONE;
            }

            var category = getOrCreateCategory(registry, String.valueOf(key), iconMaterial);

            var itemNameList = source.getList(key).asList(String.class);

            for (var itemName : itemNameList) {
                var optionalBoxItem = itemManager.getBoxItem(itemName);

                if (optionalBoxItem.isPresent()) {
                    category.addItem(optionalBoxItem.get());
                    uncategorizedItems.remove(optionalBoxItem.get());
                }
            }
        }

        var experimentalItems = getExperimentalItems();

        uncategorizedItems.stream()
                .filter(Predicate.not(itemManager::isCustomItem))
                .filter(item -> experimentalItems.contains(item.getOriginal().getType()))
                .sorted(Comparator.comparing(item -> item.getOriginal().getType()))
                .forEach(item -> {
                    getOrCreateCategory(registry, "experimental", Material.CHAIN_COMMAND_BLOCK, false).addItem(item);
                    uncategorizedItems.remove(item);
                });

        if (uncategorizedItems.isEmpty()) {
            return;
        }

        var defaultCategoryMap = BundledCategoryFile.loadDefaultCategoryMap(defaultCategoryFile);

        for (var item : uncategorizedItems.stream().sorted(Comparator.comparing(BoxItem::getPlainName)).toList()) {
            CommonDefaultCategory defaultCategory = null;

            for (var entry : defaultCategoryMap.entrySet()) {
                if (entry.getKey().contains(item.getPlainName())) {
                    defaultCategory = entry.getValue();
                    break;
                }
            }

            if (defaultCategory == null) {
                defaultCategory = itemManager.isCustomItem(item) ?
                        CommonDefaultCategory.CUSTOM_ITEMS :
                        CommonDefaultCategory.UNCATEGORIZED;
            }

            var category = getOrCreateCategory(registry, defaultCategory);

            category.addItem(item);

            BoxProvider.get().getLogger().info("Added uncategorized item '" + item.getPlainName() + "' to category " + defaultCategory.getName());
        }
    }

    private static @NotNull Set<Material> getExperimentalItems() {
        if (MCDataVersion.MC_1_19_3.isSame(MCDataVersion.CURRENT)) {
            return ExperimentalItems.mc1_19_3();
        } else if (MCDataVersion.MC_1_19_4.isSame(MCDataVersion.CURRENT)) {
            return ExperimentalItems.mc1_19_4();
        } else {
            return Collections.emptySet();
        }
    }

    private static @NotNull Category getOrCreateCategory(@NotNull CategoryRegistry registry, @NotNull String name, @NotNull Material icon) {
        return getOrCreateCategory(registry, name, icon, true);
    }

    private static @NotNull Category getOrCreateCategory(@NotNull CategoryRegistry registry, @NotNull String name, @NotNull Material icon, boolean shouldSave) {
        return getOrCreateCategory(registry, name, () -> Category.create(Component.translatable("box.category.name." + name), icon, shouldSave));
    }

    private static @NotNull Category getOrCreateCategory(@NotNull CategoryRegistry registry, @NotNull DefaultCategory defaultCategory) {
        return getOrCreateCategory(registry, defaultCategory.getName(), defaultCategory::toCategory);
    }

    private static @NotNull Category getOrCreateCategory(@NotNull CategoryRegistry registry, @NotNull String name, @NotNull Supplier<Category> categorySupplier) {
        var category = registry.getByName(name).orElse(null);

        if (category == null) {
            category = categorySupplier.get();
            registry.register(name, category);
        }

        return category;
    }

    public static @NotNull MapNode dump(@NotNull CategoryRegistry registry) {
        var mapNode = MapNode.create();
        var iconMap = mapNode.getOrCreateMap("icons");
        var categoryMap = registry.asMap();

        for (var entry : categoryMap.entrySet()) {
            var category = entry.getValue();

            if (!category.shouldSave()) {
                continue;
            }

            var categoryName = entry.getKey();
            iconMap.set(categoryName, category.getIconMaterial().name());
            mapNode.set(categoryName, category.getItems().stream().map(BoxItem::getPlainName).toList());
        }

        return mapNode;
    }

    private CategoryFile() {
        throw new UnsupportedOperationException();
    }
}
