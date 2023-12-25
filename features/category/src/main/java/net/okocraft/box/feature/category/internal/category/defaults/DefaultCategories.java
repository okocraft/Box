package net.okocraft.box.feature.category.internal.category.defaults;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.util.MCDataVersion;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class DefaultCategories {

    public static @NotNull @Unmodifiable List<DefaultCategory> loadDefaultCategories(@NotNull MCDataVersion current) throws IOException {
        return collectCurrentDefaultCategories(current, loadCategorizedItemNames(loadDefaultCategoriesFile()));
    }

    public static @NotNull @Unmodifiable List<DefaultCategory> loadNewItems(@NotNull MCDataVersion version, @NotNull MCDataVersion current) throws IOException {
        return collectNewItems(version, current, loadCategorizedItemNames(loadDefaultCategoriesFile()));
    }

    @VisibleForTesting
    static List<DefaultCategory> collectCurrentDefaultCategories(@NotNull MCDataVersion current, @NotNull Map<String, List<ItemNameSet>> source) {
        var result = new LinkedHashMap<String, List<String>>(source.size());

        for (var entry : source.entrySet()) {
            result.put(
                    String.valueOf(entry.getKey()),
                    entry.getValue().stream().map(set -> set.getCurrentName(current)).filter(Objects::nonNull).toList()
            );
        }

        return toDefaultCategories(result);
    }

    @VisibleForTesting
    static @NotNull @Unmodifiable List<DefaultCategory> collectNewItems(@NotNull MCDataVersion version, @NotNull MCDataVersion current, @NotNull Map<String, List<ItemNameSet>> source) {
        var result = new LinkedHashMap<String, List<String>>();

        for (var entry : source.entrySet()) {
            var items = new ArrayList<String>();
            for (var itemNameSet : entry.getValue()) {
                if (version.isBefore(itemNameSet.since()) && current.isAfterOrSame(itemNameSet.since())) {
                    items.add(Objects.requireNonNull(itemNameSet.getCurrentName(current)));
                }
            }
            if (!items.isEmpty()) {
                result.put(entry.getKey(), items);
            }
        }

        return toDefaultCategories(result);
    }

    static @NotNull Map<String, List<ItemNameSet>> loadCategorizedItemNames(@NotNull MapNode source) {
        var map = new LinkedHashMap<String, List<ItemNameSet>>(source.value().size());

        for (var key : source.value().keySet()) {
            var lines = source.getList(key).asList(String.class);
            var itemNameSetList = new ArrayList<ItemNameSet>(lines.size());

            for (var line : lines) {
                itemNameSetList.add(ItemNameSet.parse(line));
            }

            map.put(String.valueOf(key), itemNameSetList);
        }

        return map;
    }

    private static @NotNull MapNode loadDefaultCategoriesFile() throws IOException {
        try (var in = DefaultCategories.class.getClassLoader().getResourceAsStream("default_categories.yml")) {
            return YamlFormat.DEFAULT.load(Objects.requireNonNull(in));
        }
    }

    private static @NotNull List<DefaultCategory> toDefaultCategories(@NotNull Map<String, List<String>> categorizedItemMap) {
        return List.of(
                DefaultCategory.builder().key("dirt").icon(Material.DIRT).items(categorizedItemMap.get("dirt")).addDefaultDisplayName("Dirt").build(),
                DefaultCategory.builder().key("sand").icon(Material.SAND).items(categorizedItemMap.get("sand")).addDefaultDisplayName("Sand").build(),
                DefaultCategory.builder().key("stones").icon(Material.STONE).items(categorizedItemMap.get("stones")).addDefaultDisplayName("Stones").build(),
                DefaultCategory.builder().key("ores").icon(Material.IRON_ORE).items(categorizedItemMap.get("ores")).addDefaultDisplayName("Ores").build(),
                DefaultCategory.builder().key("woods").icon(Material.OAK_LOG).items(categorizedItemMap.get("woods-1")).addDefaultDisplayName("Woods").build(),
                DefaultCategory.builder().key("woods-2").icon(Material.CHERRY_LOG).items(categorizedItemMap.get("woods-2")).addDefaultDisplayName("Woods").build(),
                DefaultCategory.builder().key("decorations").icon(Material.CRAFTING_TABLE).items(categorizedItemMap.get("decorations")).addDefaultDisplayName("Decorations").build(),
                DefaultCategory.builder().key("glasses").icon(Material.GLASS).items(categorizedItemMap.get("glasses")).addDefaultDisplayName("Glasses").build(),
                DefaultCategory.builder().key("terracotta").icon(Material.TERRACOTTA).items(categorizedItemMap.get("terracotta")).addDefaultDisplayName("Terracotta").build(),
                DefaultCategory.builder().key("concretes").icon(Material.WHITE_CONCRETE).items(categorizedItemMap.get("concretes")).addDefaultDisplayName("Concretes").build(),
                DefaultCategory.builder().key("wools").icon(Material.WHITE_WOOL).items(categorizedItemMap.get("wools")).addDefaultDisplayName("Wools").build(),
                DefaultCategory.builder().key("nether").icon(Material.NETHERRACK).items(categorizedItemMap.get("nether")).addDefaultDisplayName("Nether").build(),
                DefaultCategory.builder().key("end").icon(Material.END_STONE).items(categorizedItemMap.get("end")).addDefaultDisplayName("End").build(),
                DefaultCategory.builder().key("shulker-boxes").icon(Material.SHULKER_BOX).items(categorizedItemMap.get("shulker-boxes")).addDefaultDisplayName("Shulker Boxes").build(),
                DefaultCategory.builder().key("farms").icon(Material.WHEAT).items(categorizedItemMap.get("farms")).addDefaultDisplayName("Farms").build(),
                DefaultCategory.builder().key("oceans").icon(Material.HEART_OF_THE_SEA).items(categorizedItemMap.get("oceans")).addDefaultDisplayName("Oceans").build(),
                DefaultCategory.builder().key("flowers").icon(Material.POPPY).items(categorizedItemMap.get("flowers")).addDefaultDisplayName("Flowers").build(),
                DefaultCategory.builder().key("dyes").icon(Material.RED_DYE).items(categorizedItemMap.get("dyes")).addDefaultDisplayName("Dyes").build(),
                DefaultCategory.builder().key("candles").icon(Material.CANDLE).items(categorizedItemMap.get("candles")).addDefaultDisplayName("Candles").build(),
                DefaultCategory.builder().key("decorated-pot").icon(Material.DECORATED_POT).items(categorizedItemMap.get("decorated-pot")).addDefaultDisplayName("Decorated Pot").build(),
                DefaultCategory.builder().key("redstones").icon(Material.REDSTONE).items(categorizedItemMap.get("redstones")).addDefaultDisplayName("Redstones").build(),
                DefaultCategory.builder().key("rails").icon(Material.RAIL).items(categorizedItemMap.get("rails")).addDefaultDisplayName("Rails").build(),
                DefaultCategory.builder().key("mob-drops").icon(Material.ROTTEN_FLESH).items(categorizedItemMap.get("mob-drops")).addDefaultDisplayName("Mob Drops").build(),
                DefaultCategory.builder().key("misc").icon(Material.PAPER).items(categorizedItemMap.get("misc")).addDefaultDisplayName("Misc").build(),
                DefaultCategory.builder().key("bows").icon(Material.BOW).items(categorizedItemMap.get("bows")).addDefaultDisplayName("Bows").build(),
                DefaultCategory.builder().key("tools").icon(Material.IRON_PICKAXE).items(categorizedItemMap.get("tools")).addDefaultDisplayName("Tools").build(),
                DefaultCategory.builder().key("armors").icon(Material.LEATHER_HELMET).items(categorizedItemMap.get("armors")).addDefaultDisplayName("Armors").build(),
                DefaultCategory.builder().key("enchanted-books").icon(Material.ENCHANTED_BOOK).items(categorizedItemMap.get("enchanted-books")).addDefaultDisplayName("Enchanted Books").build(),
                DefaultCategory.builder().key("potions").icon(Material.POTION).items(categorizedItemMap.get("potions")).addDefaultDisplayName("Potions").build(),
                DefaultCategory.builder().key("horse").icon(Material.SADDLE).items(categorizedItemMap.get("horse")).addDefaultDisplayName("Horse").build(),
                DefaultCategory.builder().key("music-discs").icon(Material.MUSIC_DISC_CAT).items(categorizedItemMap.get("music-discs")).addDefaultDisplayName("Music Discs").build(),
                DefaultCategory.builder().key("spawn-eggs").icon(Material.COW_SPAWN_EGG).items(categorizedItemMap.get("spawn-eggs")).addDefaultDisplayName("Spawn Eggs").build(),
                DefaultCategory.builder().key("unavailable").icon(Material.BEDROCK).items(categorizedItemMap.get("unavailable")).addDefaultDisplayName("Unavailable").build(),
                DefaultCategory.builder().key("sculk").icon(Material.SCULK).items(categorizedItemMap.get("sculk")).addDefaultDisplayName("Sculk").build());
    }

    record VersionedItemName(@NotNull MCDataVersion since, @NotNull String name) {
    }

    record ItemNameSet(@NotNull MCDataVersion since, @NotNull Collection<VersionedItemName> list) {

        public static final MCDataVersion UNKNOWN_VERSION = MCDataVersion.of(-1);
        private static final Pattern NAME_SEPARATOR = Pattern.compile(";", Pattern.LITERAL);
        private static final Pattern VERSION_SEPARATOR = Pattern.compile(":", Pattern.LITERAL);

        static @NotNull ItemNameSet parse(@NotNull String str) {
            var names = NAME_SEPARATOR.split(str);
            var result = new ArrayList<VersionedItemName>();

            MCDataVersion minVer = null;

            for (var name : names) {
                if (name.isEmpty()) {
                    continue;
                }

                var elements = VERSION_SEPARATOR.split(name);

                MCDataVersion version;
                String itemName;

                if (elements.length == 1) {
                    version = UNKNOWN_VERSION;
                    itemName = elements[0];
                } else {
                    version = MCDataVersion.of(Integer.parseInt(elements[0]));
                    itemName = elements[1];
                }

                result.add(new VersionedItemName(version, itemName));

                if (minVer == null || version.isBefore(minVer)) {
                    minVer = version;
                }
            }

            return new ItemNameSet(minVer != null ? minVer : UNKNOWN_VERSION, Collections.unmodifiableList(result));
        }

        @Nullable String getCurrentName(@NotNull MCDataVersion current) {
            return this.list.stream()
                    .filter(itemName -> current.isAfterOrSame(itemName.since()))
                    .max(Comparator.comparingInt(item -> item.since().dataVersion()))
                    .map(VersionedItemName::name)
                    .orElse(null);
        }
    }
}
