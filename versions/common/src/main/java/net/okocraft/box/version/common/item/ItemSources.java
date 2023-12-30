package net.okocraft.box.version.common.item;

import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ItemSources {

    public static final Predicate<Material> NOT_GOAT_HORN = Predicate.not(material -> material.name().equals("GOAT_HORN"));

    public static @NotNull Stream<Material> materials() {
        return Arrays.stream(Material.values())
                .filter(Predicate.not(Material::isAir))
                .filter(Material::isItem)
                .filter(Predicate.not(material -> material.name().startsWith("LEGACY_")));
    }

    public static @NotNull Predicate<Material> createEnabledItemFilter(@NotNull World world) {
        return material -> material.isEnabledByFeature(world);
    }

    public static @NotNull Stream<DefaultItem> potions() {
        return toStream(new DefaultPotionIterator());
    }

    public static @NotNull Stream<DefaultItem> enchantedBooks() {
        return toStream(Registry.ENCHANTMENT.iterator())
                .map(enchantment -> {
                    var name = ItemNameGenerator.keys(Material.ENCHANTED_BOOK, enchantment);

                    var book = new ItemStack(Material.ENCHANTED_BOOK);
                    book.editMeta(EnchantmentStorageMeta.class, meta -> meta.addStoredEnchant(enchantment, enchantment.getMaxLevel(), false));

                    return new DefaultItem(name, book);
                });
    }

    public static @NotNull Stream<DefaultItem> fireworks() {
        return Stream.of(1, 2, 3).map(power -> {
            var name = ItemNameGenerator.key(Material.FIREWORK_ROCKET) + "_" + power;
            var firework = new ItemStack(Material.FIREWORK_ROCKET);
            firework.editMeta(FireworkMeta.class, meta -> meta.setPower(power));

            return new DefaultItem(name, firework);
        });
    }

    public static @NotNull Stream<DefaultItem> goatHorns() {
        return Registry.INSTRUMENT.stream()
                .map(instrument -> {
                    var name = ItemNameGenerator.key(instrument);
                    var goatHorn = new ItemStack(Material.GOAT_HORN);
                    goatHorn.editMeta(MusicInstrumentMeta.class, meta -> meta.setInstrument(instrument));
                    return new DefaultItem(name, goatHorn);
                });
    }

    public static @NotNull DefaultItem toDefaultItem(@NotNull Material material) {
        return new DefaultItem(ItemNameGenerator.key(material), new ItemStack(material, 1));
    }

    private static <T> @NotNull Stream<T> toStream(@NotNull Iterator<? extends T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.DISTINCT), false);
    }

    public static class Merger {
        private Stream<DefaultItem> current;

        public @NotNull Merger append(@NotNull Stream<DefaultItem> stream) {
            if (this.current != null) {
                this.current = Stream.concat(this.current, stream);
            } else {
                this.current = stream;
            }
            return this;
        }

        public @NotNull Stream<DefaultItem> result() {
            if (this.current == null) {
                throw new IllegalStateException("No stream supplied");
            }
            return this.current;
        }
    }

    private ItemSources() {
        throw new UnsupportedOperationException();
    }

}
