package net.okocraft.box.storage.api.model.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class DefaultItemProvider {

    static @NotNull List<DefaultItem> fromMaterials() {
        return Arrays.stream(Material.values())
                .filter(Predicate.not(Material::isAir))
                .filter(Material::isItem)
                .filter(Predicate.not(material -> material.name().startsWith("LEGACY_")))
                .map(material -> new DefaultItem(material.name(), new ItemStack(material, 1)))
                .collect(Collectors.toList());
    }

    static @NotNull List<DefaultItem> potions() {
        var result = new ArrayList<DefaultItem>(175); // current: 168 potions

        var potions =
                List.of(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);

        for (var type : PotionType.values()) {
            if (type == PotionType.UNCRAFTABLE) {
                continue;
            }

            var normalData = new PotionData(type, false, false);
            createPotions(potions, normalData, result);

            if (type.isExtendable()) {
                var extendedData = new PotionData(type, true, false);
                createPotions(potions, extendedData, result);
            }

            if (type.isUpgradeable()) {
                var upgradedData = new PotionData(type, false, true);
                createPotions(potions, upgradedData, result);
            }
        }

        return result;
    }

    private static void createPotions(@NotNull List<Material> materials,
                                      @NotNull PotionData data, @NotNull List<DefaultItem> result) {
        if (Bukkit.getItemFactory().getItemMeta(Material.POTION) instanceof PotionMeta meta) {
            meta.setBasePotionData(data);
            var potions = materials.stream().map(ItemStack::new).toList();

            potions.forEach(item -> item.setItemMeta(meta.clone()));

            for (var potion : potions) {
                String name = potion.getType().name() + "_" + data.getType().name();

                if (data.isExtended()) {
                    name = name + "_EXTENDED";
                } else if (data.isUpgraded()) {
                    name = name + "_UPGRADED";
                }

                result.add(new DefaultItem(name, potion));
            }
        } else {
            throw new IllegalStateException("Where has PotionMeta gone!?");
        }
    }

    static @NotNull List<DefaultItem> enchantedBooks() {
        if (Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK) instanceof EnchantmentStorageMeta meta) {
            var result = new ArrayList<DefaultItem>(40); // current: 37 enchanted books

            for (var enchant : Enchantment.values()) {
                var cloned = meta.clone();
                cloned.addStoredEnchant(enchant, enchant.getMaxLevel(), false);

                var book = new ItemStack(Material.ENCHANTED_BOOK);
                book.setItemMeta(cloned);

                var name = book.getType().name() + "_" + enchant.getKey().getKey().toUpperCase(Locale.ROOT);
                result.add(new DefaultItem(name, book));
            }

            return result;
        } else {
            throw new IllegalStateException("Where has EnchantmentStorageMeta gone!?");
        }
    }

    static @NotNull List<DefaultItem> fireworks() {
        return Stream.of(1, 2, 3)
                .map(DefaultItemProvider::createFirework)
                .collect(Collectors.toList());
    }

    private static @NotNull DefaultItem createFirework(int power) {
        var meta = Bukkit.getItemFactory().getItemMeta(Material.FIREWORK_ROCKET);

        if (!(meta instanceof FireworkMeta fireworkMeta)) {
            throw new IllegalStateException("Where has FireworkMeta gone!?");
        }

        fireworkMeta.setPower(power);

        var firework = new ItemStack(Material.FIREWORK_ROCKET);
        firework.setItemMeta(fireworkMeta);

        return new DefaultItem(Material.FIREWORK_ROCKET + "_" + power, firework);
    }

    public record DefaultItem(@NotNull String plainName, @NotNull ItemStack itemStack) {
    }
}
