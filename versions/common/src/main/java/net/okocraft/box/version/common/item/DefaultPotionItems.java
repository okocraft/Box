package net.okocraft.box.version.common.item;

import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class DefaultPotionItems {

    @Contract("_ -> new")
    static @NotNull Stream<DefaultItem> stream(@NotNull Registry<PotionType> registry) {
        return StreamSupport.stream(new PotionItemSpliterator(registry.stream().toArray(PotionType[]::new)), false);
    }

    private DefaultPotionItems() {
        throw new UnsupportedOperationException();
    }

    private static final class PotionItemSpliterator implements Spliterator<DefaultItem> {

        private static final Material[] ITEMS = {Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW};

        private final PotionType[] potionTypes;
        private final long size;

        private int potionTypeIndex = 0;
        private int itemIndex = 0;

        private PotionItemSpliterator(@NotNull PotionType[] potionTypes) {
            this.potionTypes = potionTypes;
            this.size = (long) ITEMS.length * potionTypes.length;
        }

        @Override
        public boolean tryAdvance(Consumer<? super DefaultItem> action) {
            var item = ITEMS[this.itemIndex];
            var potionType = this.potionTypes[this.potionTypeIndex];
            boolean hasNext;

            if (++this.itemIndex == ITEMS.length) {
                if (++this.potionTypeIndex == this.potionTypes.length) {
                    hasNext = false;
                } else {
                    this.itemIndex = 0;
                    hasNext = true;
                }
            } else {
                hasNext = true;
            }

            action.accept(createPotion(item, potionType));

            return hasNext;
        }

        @Override
        public Spliterator<DefaultItem> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return this.size;
        }

        @Override
        public int characteristics() {
            return NONNULL | SIZED | ORDERED;
        }

        private static @NotNull DefaultItem createPotion(@NotNull Material material, @NotNull PotionType type) {
            var name = ItemNameGenerator.keys(material, type);
            var potion = new ItemStack(material);
            potion.editMeta(PotionMeta.class, meta -> meta.setBasePotionType(type));
            return new DefaultItem(name, potion);
        }
    }
}
