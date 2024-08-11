package net.okocraft.box.version.common.item;

import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ItemSources {

    @SuppressWarnings("deprecation")
    public static @NotNull Stream<DefaultItem> itemTypes(@NotNull Registry<ItemType> registry) {
        var world = Bukkit.getWorlds().getFirst();
        return registry.stream()
                .filter(type -> type != ItemType.AIR && type != ItemType.GOAT_HORN && type != ItemType.FIREWORK_ROCKET)
                .filter(type -> type.isEnabledByFeature(world))
                .map(type -> new DefaultItem(ItemNameGenerator.key(type) , ItemStack.of(type.asMaterial(), 1))); // FIXME: remove asMaterial in the future
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
