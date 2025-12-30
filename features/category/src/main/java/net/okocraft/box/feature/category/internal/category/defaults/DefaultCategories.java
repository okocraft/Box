package net.okocraft.box.feature.category.internal.category.defaults;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.util.MCDataVersion;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Locale.JAPANESE;

public final class DefaultCategories {

    public static @NotNull @Unmodifiable List<DefaultCategory> loadDefaultCategories(@NotNull MCDataVersion current) throws IOException {
        return collectCurrentDefaultCategories(current, loadCategorizedItemNames(loadDefaultCategoriesFile()));
    }

    public static @NotNull @Unmodifiable List<DefaultCategory> loadNewItems(@NotNull MCDataVersion version, @NotNull MCDataVersion current) throws IOException {
        return collectNewItems(version, current, loadCategorizedItemNames(loadDefaultCategoriesFile()));
    }

    @VisibleForTesting
    static List<DefaultCategory> collectCurrentDefaultCategories(@NotNull MCDataVersion current, @NotNull Map<String, List<ItemNameSet>> source) {
        Map<String, List<String>> result = new LinkedHashMap<>(source.size());

        for (Map.Entry<String, List<ItemNameSet>> entry : source.entrySet()) {
            result.put(
                String.valueOf(entry.getKey()),
                entry.getValue().stream().map(set -> set.getCurrentName(current)).filter(Objects::nonNull).toList()
            );
        }

        return toDefaultCategories(result);
    }

    @VisibleForTesting
    static @NotNull @Unmodifiable List<DefaultCategory> collectNewItems(@NotNull MCDataVersion version, @NotNull MCDataVersion current, @NotNull Map<String, List<ItemNameSet>> source) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<ItemNameSet>> entry : source.entrySet()) {
            List<String> items = new ArrayList<>();
            for (ItemNameSet itemNameSet : entry.getValue()) {
                if (version.isBefore(itemNameSet.since()) && (current.isAfter(itemNameSet.since()) || current.isSame(itemNameSet.since()))) {
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
        Map<String, List<ItemNameSet>> map = new LinkedHashMap<>(source.value().size());

        for (Object key : source.value().keySet()) {
            List<String> lines = source.getList(key).asList(String.class);
            List<ItemNameSet> itemNameSetList = new ArrayList<>(lines.size());

            for (String line : lines) {
                itemNameSetList.add(ItemNameSet.parse(line));
            }

            map.put(String.valueOf(key), itemNameSetList);
        }

        return map;
    }

    private static @NotNull MapNode loadDefaultCategoriesFile() throws IOException {
        try (InputStream in = DefaultCategories.class.getClassLoader().getResourceAsStream("default_categories.yml")) {
            return YamlFormat.DEFAULT.load(Objects.requireNonNull(in));
        }
    }

    private static @NotNull List<DefaultCategory> toDefaultCategories(@NotNull Map<String, List<String>> categorizedItemMap) {
        return List.of(
            DefaultCategory.builder().key("dirt").icon(Material.DIRT).items(categorizedItemMap.get("dirt")).addDefaultDisplayName("Dirt").addDisplayName(JAPANESE, "土類").build(),
            DefaultCategory.builder().key("sand").icon(Material.SAND).items(categorizedItemMap.get("sand")).addDefaultDisplayName("Sand").addDisplayName(JAPANESE, "砂類").build(),
            DefaultCategory.builder().key("stones").icon(Material.STONE).items(categorizedItemMap.get("stones")).addDefaultDisplayName("Stones").addDisplayName(JAPANESE, "石類").build(),
            DefaultCategory.builder().key("ores").icon(Material.IRON_ORE).items(categorizedItemMap.get("ores")).addDefaultDisplayName("Ores").addDisplayName(JAPANESE, "鉱石").build(),
            DefaultCategory.builder().key("woods").icon(Material.OAK_LOG).items(categorizedItemMap.get("woods-1")).addDefaultDisplayName("Woods").addDisplayName(JAPANESE, "木材").build(),
            DefaultCategory.builder().key("woods-2").icon(Material.CHERRY_LOG).items(categorizedItemMap.get("woods-2")).addDefaultDisplayName("Woods").addDisplayName(JAPANESE, "木材").build(),
            DefaultCategory.builder().key("decorations").icon(Material.CRAFTING_TABLE).items(categorizedItemMap.get("decorations")).addDefaultDisplayName("Decorations").addDisplayName(JAPANESE, "装飾アイテム").build(),
            DefaultCategory.builder().key("glasses").icon(Material.GLASS).items(categorizedItemMap.get("glasses")).addDefaultDisplayName("Glasses").addDisplayName(JAPANESE, "ガラス").build(),
            DefaultCategory.builder().key("terracotta").icon(Material.TERRACOTTA).items(categorizedItemMap.get("terracotta")).addDefaultDisplayName("Terracotta").addDisplayName(JAPANESE, "テラコッタ").build(),
            DefaultCategory.builder().key("concretes").icon(Material.WHITE_CONCRETE).items(categorizedItemMap.get("concretes")).addDefaultDisplayName("Concretes").addDisplayName(JAPANESE, "コンクリート").build(),
            DefaultCategory.builder().key("wools").icon(Material.WHITE_WOOL).items(categorizedItemMap.get("wools")).addDefaultDisplayName("Wools").addDisplayName(JAPANESE, "羊毛").build(),
            DefaultCategory.builder().key("sculk").icon(Material.SCULK).items(categorizedItemMap.get("sculk")).addDefaultDisplayName("Sculk").addDisplayName(JAPANESE, "スカルク").build(),
            DefaultCategory.builder().key("nether").icon(Material.NETHERRACK).items(categorizedItemMap.get("nether")).addDefaultDisplayName("Nether").addDisplayName(JAPANESE, "ネザー").build(),
            DefaultCategory.builder().key("end").icon(Material.END_STONE).items(categorizedItemMap.get("end")).addDefaultDisplayName("End").addDisplayName(JAPANESE, "エンド").build(),
            DefaultCategory.builder().key("shulker-boxes").icon(Material.SHULKER_BOX).items(categorizedItemMap.get("shulker-boxes")).addDefaultDisplayName("Shulker Boxes").addDisplayName(JAPANESE, "シュルカーボックス").build(),
            DefaultCategory.builder().key("farms").icon(Material.WHEAT).items(categorizedItemMap.get("farms")).addDefaultDisplayName("Farms").addDisplayName(JAPANESE, "農業・食料").build(),
            DefaultCategory.builder().key("oceans").icon(Material.HEART_OF_THE_SEA).items(categorizedItemMap.get("oceans")).addDefaultDisplayName("Oceans").addDisplayName(JAPANESE, "海類").build(),
            DefaultCategory.builder().key("flowers").icon(Material.POPPY).items(categorizedItemMap.get("flowers")).addDefaultDisplayName("Flowers").addDisplayName(JAPANESE, "草花").build(),
            DefaultCategory.builder().key("dyes").icon(Material.RED_DYE).items(categorizedItemMap.get("dyes")).addDefaultDisplayName("Dyes").addDisplayName(JAPANESE, "染料").build(),
            DefaultCategory.builder().key("candles").icon(Material.CANDLE).items(categorizedItemMap.get("candles")).addDefaultDisplayName("Candles").addDisplayName(JAPANESE, "ろうそく").build(),
            DefaultCategory.builder().key("decorated-pot").icon(Material.DECORATED_POT).items(categorizedItemMap.get("decorated-pot")).addDefaultDisplayName("Decorated Pots").addDisplayName(JAPANESE, "飾り壺").build(),
            DefaultCategory.builder().key("redstones").icon(Material.REDSTONE).items(categorizedItemMap.get("redstones")).addDefaultDisplayName("Redstones").addDisplayName(JAPANESE, "レッドストーン").build(),
            DefaultCategory.builder().key("rails").icon(Material.RAIL).items(categorizedItemMap.get("rails")).addDefaultDisplayName("Rails").addDisplayName(JAPANESE, "レール").build(),
            DefaultCategory.builder().key("mob-drops").icon(Material.ROTTEN_FLESH).items(categorizedItemMap.get("mob-drops")).addDefaultDisplayName("Mob Drops").addDisplayName(JAPANESE, "モブドロップ品").build(),
            DefaultCategory.builder().key("misc").icon(Material.PAPER).items(categorizedItemMap.get("misc")).addDefaultDisplayName("Misc").addDisplayName(JAPANESE, "その他").build(),
            DefaultCategory.builder().key("bows").icon(Material.BOW).items(categorizedItemMap.get("bows")).addDefaultDisplayName("Bows").addDisplayName(JAPANESE, "弓矢").build(),
            DefaultCategory.builder().key("tools").icon(Material.IRON_PICKAXE).items(categorizedItemMap.get("tools")).addDefaultDisplayName("Tools").addDisplayName(JAPANESE, "ツール").build(),
            DefaultCategory.builder().key("armors").icon(Material.LEATHER_HELMET).items(categorizedItemMap.get("armors")).addDefaultDisplayName("Armors").addDisplayName(JAPANESE, "防具").build(),
            DefaultCategory.builder().key("enchanted-books").icon(Material.ENCHANTED_BOOK).items(categorizedItemMap.get("enchanted-books")).addDefaultDisplayName("Enchanted Books").addDisplayName(JAPANESE, "エンチャント本").build(),
            DefaultCategory.builder().key("potions").icon(Material.POTION).items(categorizedItemMap.get("potions")).addDefaultDisplayName("Potions").addDisplayName(JAPANESE, "ポーション").build(),
            DefaultCategory.builder().key("mob-equipment").icon(Material.SADDLE).items(categorizedItemMap.get("mob-equipment")).addDefaultDisplayName("Mob Equipment").addDisplayName(JAPANESE, "モブ用装備").build(),
            DefaultCategory.builder().key("music-discs").icon(Material.MUSIC_DISC_CAT).items(categorizedItemMap.get("music-discs")).addDefaultDisplayName("Music Discs").addDisplayName(JAPANESE, "ディスク").build(),
            DefaultCategory.builder().key("spawn-eggs").icon(Material.COW_SPAWN_EGG).items(categorizedItemMap.get("spawn-eggs")).addDefaultDisplayName("Spawn Eggs").addDisplayName(JAPANESE, "スポーンエッグ").build(),
            DefaultCategory.builder().key("unavailable").icon(Material.BEDROCK).items(categorizedItemMap.get("unavailable")).addDefaultDisplayName("Unavailable").addDisplayName(JAPANESE, "入手不可").build()
        );
    }

    record VersionedItemName(@NotNull MCDataVersion since, @NotNull String name) {
    }

    record ItemNameSet(@NotNull MCDataVersion since, @NotNull Collection<VersionedItemName> list) {

        public static final MCDataVersion UNKNOWN_VERSION = MCDataVersion.of(-1);

        static @NotNull ItemNameSet parse(@NotNull String str) {
            String[] names = split(str, ';');
            List<VersionedItemName> result = new ArrayList<>();

            MCDataVersion minVer = null;

            for (String name : names) {
                if (name.isEmpty()) {
                    continue;
                }

                String[] elements = split(name, ':');

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
                .max(Comparator.comparing(VersionedItemName::since))
                .map(VersionedItemName::name)
                .orElse(null);
        }

        private static @NotNull String @NotNull [] split(@NotNull String str, char separator) {
            int index = str.indexOf(separator);
            return index == -1 ? new String[]{str} : new String[]{str.substring(0, index), str.substring(index + 1)};
        }
    }
}
