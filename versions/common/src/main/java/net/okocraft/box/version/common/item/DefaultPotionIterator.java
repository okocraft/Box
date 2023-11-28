package net.okocraft.box.version.common.item;

import net.okocraft.box.api.util.ItemNameGenerator;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

class DefaultPotionIterator extends DefaultItemIterator {

    private static final PotionType[] POTION_TYPES;
    private static final Material[] ITEMS = {Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW};

    static {
        int index = 0;
        var values = PotionType.values();
        POTION_TYPES = new PotionType[values.length - 1];

        for (var potionType : values) {
            if (potionType == PotionType.UNCRAFTABLE) {
                continue;
            }
            POTION_TYPES[index++] = potionType;
        }
    }

    private final PotionMeta potionMeta = createItemMeta(Material.POTION, PotionMeta.class);

    private int potionTypeIndex = 0;
    private int itemIndex = 0;
    private boolean hasNext = true;

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public @NotNull DefaultItem next() {
        var potionType = POTION_TYPES[this.potionTypeIndex];
        var item = ITEMS[this.itemIndex];

        if (++this.itemIndex == ITEMS.length) {
            if (++potionTypeIndex == POTION_TYPES.length) {
                this.hasNext = false;
            } else {
                this.itemIndex = 0;
            }
        }

        return this.createPotion(item, potionType);
    }

    private @NotNull DefaultItem createPotion(@NotNull Material material, @NotNull PotionType type) {
        var name = ItemNameGenerator.keys(material, type);
        var potion = new ItemStack(material, 1);

        this.potionMeta.setBasePotionType(type);
        potion.setItemMeta(this.potionMeta);

        return new DefaultItem(name, potion);
    }
}
