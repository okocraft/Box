package net.okocraft.box.version.common.item;

import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ItemSources {

    public static final Predicate<Material> NOT_GOAT_HORN = Predicate.not(material -> material.name().equals("GOAT_HORN"));
    public static final Predicate<Material> NOT_FIREWORK = Predicate.not(material -> material.name().equals("FIREWORK_ROCKET"));

    public static @NotNull Stream<Material> materials(@NotNull Registry<Material> registry) {
        return registry.stream()
                .filter(Predicate.not(Material::isAir))
                .filter(Material::isItem)
                .filter(Predicate.not(material -> material.name().startsWith("LEGACY_")));
    }

    public static @NotNull Predicate<Material> createEnabledItemFilter(@NotNull World world) {
        return material -> material.isEnabledByFeature(world);
    }

    public static @NotNull Stream<DefaultItem> potions(@NotNull Registry<PotionType> registry) {
        return DefaultPotionItems.stream(registry);
    }

    public static @NotNull Stream<DefaultItem> enchantedBooks(Registry<Enchantment> registry) {
        return registry.stream()
                .map(enchantment -> {
                    var name = ItemNameGenerator.keys(Material.ENCHANTED_BOOK, enchantment);

                    var book = new ItemStack(Material.ENCHANTED_BOOK);
                    book.editMeta(EnchantmentStorageMeta.class, meta -> meta.addStoredEnchant(enchantment, enchantment.getMaxLevel(), false));

                    return new DefaultItem(name, book);
                });
    }

    public static @NotNull Stream<DefaultItem> fireworks() {
        return IntStream.of(1, 2, 3).mapToObj(power -> {
            var name = ItemNameGenerator.key(Material.FIREWORK_ROCKET) + "_" + power;
            var firework = new ItemStack(Material.FIREWORK_ROCKET);
            firework.editMeta(FireworkMeta.class, meta -> meta.setPower(power));

            return new DefaultItem(name, firework);
        });
    }

    public static @NotNull Stream<DefaultItem> goatHorns(@NotNull Registry<MusicInstrument> registry) {
        return registry.stream()
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
