package net.okocraft.box.version.common.item;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Fireworks;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.OminousBottleAmplifier;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ItemSources {

    @SuppressWarnings("deprecation")
    public static @NotNull Stream<DefaultItem> itemTypes() {
        World world = Bukkit.getWorlds().getFirst();
        Set<ItemType> excludedTypes = Set.of(ItemType.AIR, ItemType.GOAT_HORN, ItemType.FIREWORK_ROCKET, ItemType.OMINOUS_BOTTLE);
        return registry(RegistryKey.ITEM).stream()
            .filter(Predicate.not(excludedTypes::contains))
            .filter(world::isEnabled)
            .map(type -> new DefaultItem(ItemNameGenerator.key(type), ItemStack.of(type.asMaterial(), 1))); // FIXME: remove asMaterial in the future
    }

    public static @NotNull Stream<DefaultItem> potions() {
        return registry(RegistryKey.POTION).stream().flatMap(ItemSources::createItemsForPotionType);
    }

    private static @NotNull Stream<DefaultItem> createItemsForPotionType(@NotNull PotionType potionType) {
        return Stream.of(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW)
            .map(itemType -> {
                String name = ItemNameGenerator.keys(itemType, potionType);
                ItemStack item = ItemStack.of(itemType, 1);
                item.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().potion(potionType));
                return new DefaultItem(name, item);
            });
    }

    public static @NotNull Stream<DefaultItem> enchantedBooks() {
        return registry(RegistryKey.ENCHANTMENT).stream()
            .map(enchantment -> {
                String name = ItemNameGenerator.keys(Material.ENCHANTED_BOOK, enchantment);
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                book.setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments().add(enchantment, enchantment.getMaxLevel()));
                return new DefaultItem(name, book);
            });
    }

    public static @NotNull Stream<DefaultItem> fireworks() {
        return IntStream.of(1, 2, 3).mapToObj(power -> {
            String name = ItemNameGenerator.key(Material.FIREWORK_ROCKET) + "_" + power;
            ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
            firework.setData(DataComponentTypes.FIREWORKS, Fireworks.fireworks().flightDuration(power));
            return new DefaultItem(name, firework);
        });
    }

    public static @NotNull Stream<DefaultItem> goatHorns() {
        return registry(RegistryKey.INSTRUMENT).stream().map(instrument -> {
            String name = ItemNameGenerator.key(instrument);
            ItemStack goatHorn = new ItemStack(Material.GOAT_HORN);
            goatHorn.setData(DataComponentTypes.INSTRUMENT, instrument);
            return new DefaultItem(name, goatHorn);
        });
    }

    public static @NotNull Stream<DefaultItem> ominousBottles() {
        return IntStream.rangeClosed(1, 5).mapToObj(level -> {
            String name = ItemNameGenerator.key(Material.OMINOUS_BOTTLE) + "_" + level;
            ItemStack bottle = new ItemStack(Material.OMINOUS_BOTTLE);
            bottle.setData(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, OminousBottleAmplifier.amplifier(level - 1));
            return new DefaultItem(name, bottle);
        });
    }

    private static <T extends Keyed> @NotNull Registry<@NotNull T> registry(@NotNull RegistryKey<@NotNull T> key) {
        return RegistryAccess.registryAccess().getRegistry(key);
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
