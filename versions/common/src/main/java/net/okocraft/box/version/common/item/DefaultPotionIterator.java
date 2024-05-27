package net.okocraft.box.version.common.item;

import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

class DefaultPotionIterator implements Iterator<DefaultItem> {

    private static final Material[] ITEMS = {Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW};

    private final PotionType[] potionTypes;

    private int potionTypeIndex = 0;
    private int itemIndex = 0;
    private boolean hasNext = true;

    DefaultPotionIterator(@NotNull Registry<PotionType> registry) {
        this.potionTypes = registry.stream().toArray(PotionType[]::new);
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public @NotNull DefaultItem next() {
        var potionType = this.potionTypes[this.potionTypeIndex];
        var item = ITEMS[this.itemIndex];

        if (++this.itemIndex == ITEMS.length) {
            if (++this.potionTypeIndex == this.potionTypes.length) {
                this.hasNext = false;
            } else {
                this.itemIndex = 0;
            }
        }

        return this.createPotion(item, potionType);
    }

    private @NotNull DefaultItem createPotion(@NotNull Material material, @NotNull PotionType type) {
        var name = ItemNameGenerator.keys(material, type);
        var potion = new ItemStack(material);

        potion.editMeta(PotionMeta.class, meta -> meta.setBasePotionType(type));

        return new DefaultItem(name, potion);
    }
}
