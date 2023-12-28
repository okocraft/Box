package net.okocraft.box.feature.category.internal.file;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.node.StringRepresentable;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.category.CustomItemCategory;
import net.okocraft.box.feature.category.internal.category.LoadedCategory;
import net.okocraft.box.feature.category.internal.category.defaults.DefaultCategories;
import net.okocraft.box.feature.category.internal.category.defaults.DefaultCategory;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

public final class CategoryFile implements AutoCloseable {

    public static final String VERSION_KEY = "$version";
    public static final String ITEMS_KEY = "items";
    public static final String ICON_KEY = "icon";
    public static final String DISPLAY_NAME_KEY = "display-name";
    public static final String LOCALE_DEFAULT = "default";

    private final Path filepath;
    private final CategoryRegistry registry;
    private final ItemManager itemManager;

    private MapNode loadedSource;
    private MCDataVersion version;

    public CategoryFile(@NotNull Path filepath, @NotNull CategoryRegistry registry, @NotNull ItemManager itemManager) {
        this.filepath = filepath;
        this.registry = registry;
        this.itemManager = itemManager;
    }

    @Override
    public void close() {
        this.loadedSource = null;
    }

    public CategoryFile loadFile() throws IOException {
        if (Files.isRegularFile(this.filepath)) {
            this.loadedSource = YamlFormat.COMMENT_PROCESSING.load(this.filepath);
            this.version = MCDataVersion.of(this.loadedSource.getInteger(VERSION_KEY));
        }
        return this;
    }

    public CategoryFile convertIfUnknownVersion() throws IOException {
        if (this.version == null || this.version.dataVersion() != 0) {
            return this;
        }

        Files.copy(this.filepath, this.filepath.getParent().resolve(this.filepath.getFileName().toString() + ".backup-" + System.currentTimeMillis()));

        var mapNode = MapNode.create();

        for (var key : this.loadedSource.value().keySet()) {
            if (key.equals("icons")) {
                continue;
            } else if (key.equals("custom-items")) {
                var newList = mapNode.createList(CustomItemCategory.KEY);
                this.loadedSource.getList(key).value().forEach(newList::add);
                continue;
            }

            var map = mapNode.createMap(renameKey(key));
            map.set(ICON_KEY, this.loadedSource.getMap("icons").getString(key));
            addDefaultDisplayName(String.valueOf(key), map);
            map.set(ITEMS_KEY, this.loadedSource.getList(key));
        }

        this.loadedSource = mapNode;
        return this;
    }

    public CategoryFile readCategoriesIfExists() {
        if (this.loadedSource == null) return this;

        for (var entry : this.loadedSource.value().entrySet()) {
            var key = String.valueOf(entry.getKey());
            if (!key.startsWith("$") && entry.getValue() instanceof MapNode section) {
                this.registry.register(key, loadCategory(key, section, UnaryOperator.identity()));
            }
        }

        return this;
    }

    public CategoryFile readCustomItemsIfExists(@NotNull CustomItemCategory category) {
        if (this.loadedSource != null) {
            category.addItems(this.toBoxItems(this.loadedSource.getList(CustomItemCategory.KEY).asList(String.class)));
        }
        return this;
    }

    public void addNewDefaultItemsIfNeeded() throws IOException {
        MapNode mapNode;
        List<DefaultCategory> defaultCategories;

        if (this.version == null) {
            mapNode = MapNode.create();
            defaultCategories = DefaultCategories.loadDefaultCategories(MCDataVersion.CURRENT);
        } else if (MCDataVersion.CURRENT.isAfter(this.version)) {
            mapNode = Objects.requireNonNull(this.loadedSource);
            defaultCategories = DefaultCategories.loadNewItems(this.version, MCDataVersion.CURRENT);
        } else {
            return;
        }

        for (var category : defaultCategories) {
            var items = category.itemNames();

            if (items.isEmpty()) {
                continue;
            }

            this.registry.getByName(category.key()).ifPresentOrElse(
                    target -> {
                        target.addItems(this.toBoxItems(items));
                        var list = mapNode.getOrCreateMap(category.key()).getOrCreateList(ITEMS_KEY);
                        items.forEach(list::add);
                    },
                    () -> {
                        this.registry.register(category.key(), category.toCategory(this.itemManager));
                        category.storeToMapNode(mapNode.createMap(category.key()));
                    }
            );
        }

        mapNode.set(VERSION_KEY, MCDataVersion.CURRENT.dataVersion());
        YamlFormat.COMMENT_PROCESSING.save(mapNode, this.filepath);
    }

    private static LoadedCategory loadCategory(@NotNull String key, @NotNull MapNode source, @NotNull UnaryOperator<String> itemNameConverter) {
        Material iconMaterial;

        try {
            iconMaterial = Material.valueOf(itemNameConverter.apply(source.getString(ICON_KEY)));
        } catch (IllegalArgumentException e) {
            BoxLogger.logger().warn("Unknown icon in '{}': {}", key, source.getString(ICON_KEY));
            iconMaterial = Material.STONE;
        }

        Map<Locale, String> displayNameMap;

        if (source.get(DISPLAY_NAME_KEY) instanceof MapNode mapNode) {
            var map = new HashMap<Locale, String>();

            for (var entry : mapNode.value().entrySet()) {
                if (entry.getKey() instanceof String locale && entry.getValue() instanceof StringRepresentable stringRepresentable) {
                    if (locale.equalsIgnoreCase(LOCALE_DEFAULT)) {
                        map.put(null, stringRepresentable.asString());
                    } else {
                        map.put(new Locale(locale), stringRepresentable.asString());
                    }
                }
            }

            displayNameMap = map;
        } else {
            displayNameMap = Collections.emptyMap();
        }

        var itemNameList = source.getList(ITEMS_KEY).asList(String.class);
        var items = new ArrayList<BoxItem>(itemNameList.size());

        for (var name : itemNameList) {
            BoxAPI.api().getItemManager().getBoxItem(itemNameConverter.apply(name)).ifPresent(items::add);
        }

        var category = new LoadedCategory(iconMaterial, displayNameMap);
        category.addItems(items);
        return category;
    }

    private @NotNull Collection<BoxItem> toBoxItems(@NotNull List<String> itemNames) {
        return itemNames.stream().map(this.itemManager::getBoxItem).filter(Optional::isPresent).map(Optional::get).toList();
    }

    private static Object renameKey(@NotNull Object key) {
        if (key.equals("woods")) {
            return "woods-1";
        }
        return key;
    }

    private static void addDefaultDisplayName(@NotNull String key, @NotNull MapNode target) {
        Map<String, String> displayNameMap = switch (key) {
            case "armors" -> Map.of(LOCALE_DEFAULT, "Armors", "ja", "防具");
            case "bows" -> Map.of(LOCALE_DEFAULT, "Bows", "ja", "弓矢");
            case "candles" -> Map.of(LOCALE_DEFAULT, "Candles", "ja", "ろうそく");
            case "concretes" -> Map.of(LOCALE_DEFAULT, "Concretes", "ja", "コンクリート");
            case "custom-items" -> Map.of(LOCALE_DEFAULT, "Custom Items", "ja", "カスタムアイテム");
            case "decorated-pot" -> Map.of(LOCALE_DEFAULT, "Decorated Pots", "ja", "飾り壺");
            case "decorations" -> Map.of(LOCALE_DEFAULT, "Decorations", "ja", "装飾アイテム");
            case "dirt" -> Map.of(LOCALE_DEFAULT, "Dirt", "ja", "土類");
            case "dyes" -> Map.of(LOCALE_DEFAULT, "Dyes", "ja", "染料");
            case "enchanted-books" -> Map.of(LOCALE_DEFAULT, "Enchanted Books", "ja", "エンチャント本");
            case "end" -> Map.of(LOCALE_DEFAULT, "End", "ja", "エンド");
            case "experimental" -> Map.of(LOCALE_DEFAULT, "Experimental Items", "ja", "実験的アイテム");
            case "farms" -> Map.of(LOCALE_DEFAULT, "Farms", "ja", "農業・食料");
            case "flowers" -> Map.of(LOCALE_DEFAULT, "Flowers", "ja", "草花");
            case "glasses" -> Map.of(LOCALE_DEFAULT, "Glasses", "ja", "ガラス");
            case "horse" -> Map.of(LOCALE_DEFAULT, "Horse", "ja", "馬");
            case "misc" -> Map.of(LOCALE_DEFAULT, "Misc", "ja", "その他");
            case "mob-drops" -> Map.of(LOCALE_DEFAULT, "Mob Drops", "ja", "モブドロップ品");
            case "mushrooms" -> Map.of(LOCALE_DEFAULT, "Mushrooms", "ja", "きのこ");
            case "music-discs" -> Map.of(LOCALE_DEFAULT, "Music Discs", "ja", "ディスク");
            case "nether" -> Map.of(LOCALE_DEFAULT, "Nether", "ja", "ネザー");
            case "oceans" -> Map.of(LOCALE_DEFAULT, "Oceans", "ja", "海類");
            case "ores" -> Map.of(LOCALE_DEFAULT, "Ores", "ja", "鉱石");
            case "potions" -> Map.of(LOCALE_DEFAULT, "Potions", "ja", "ポーション");
            case "rails" -> Map.of(LOCALE_DEFAULT, "Rails", "ja", "レール");
            case "redstones" -> Map.of(LOCALE_DEFAULT, "Redstones", "ja", "レッドストーン");
            case "sand" -> Map.of(LOCALE_DEFAULT, "Sand", "ja", "砂類");
            case "sculk" -> Map.of(LOCALE_DEFAULT, "Sculk", "ja", "スカルク");
            case "shulker-boxes" -> Map.of(LOCALE_DEFAULT, "Shulker Boxes", "ja", "シュルカーボックス");
            case "spawn-eggs" -> Map.of(LOCALE_DEFAULT, "Spawn Eggs", "ja", "スポーンエッグ");
            case "stones" -> Map.of(LOCALE_DEFAULT, "Stones", "ja", "石類");
            case "terracotta" -> Map.of(LOCALE_DEFAULT, "Terracotta", "ja", "テラコッタ");
            case "tools" -> Map.of(LOCALE_DEFAULT, "Tools", "ja", "ツール");
            case "unavailable" -> Map.of(LOCALE_DEFAULT, "Unavailable", "ja", "入手不可");
            case "uncategorized" -> Map.of(LOCALE_DEFAULT, "Uncategorized", "ja", "未分類");
            case "woods" -> Map.of(LOCALE_DEFAULT, "Woods", "ja", "木材");
            case "woods-2" -> Map.of(LOCALE_DEFAULT, "Woods", "ja", "木材");
            case "wools" -> Map.of(LOCALE_DEFAULT, "Wools", "ja", "羊毛");
            default -> null;
        };

        if (displayNameMap != null) {
            target.set(DISPLAY_NAME_KEY, displayNameMap);
        }
    }
}
