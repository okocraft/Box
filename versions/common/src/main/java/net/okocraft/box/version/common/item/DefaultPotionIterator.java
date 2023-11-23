package net.okocraft.box.version.common.item;

import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
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
    private int potionDataIndex = 0;
    private int itemIndex = 0;
    private boolean hasNext = true;

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public @NotNull DefaultItem next() {
        var potionData = new PotionData(POTION_TYPES[this.potionTypeIndex], this.potionDataIndex == 1, this.potionDataIndex == 2);
        var item = ITEMS[this.itemIndex];

        if (++this.itemIndex == ITEMS.length) {
            if (++this.potionDataIndex == 3) {
                if (++potionTypeIndex == POTION_TYPES.length) {
                    this.hasNext = false;
                } else {
                    this.potionDataIndex = 0;
                    this.itemIndex = 0;
                }
            } else {
                this.itemIndex = 0;
            }
        }

        return this.createPotion(potionData, item);
    }

    private @NotNull DefaultItem createPotion(@NotNull PotionData data, @NotNull Material material) {
        var name = material.name() + "_" + data.getType().name();

        if (data.isExtended()) {
            name = name + "_EXTENDED";
        } else if (data.isUpgraded()) {
            name = name + "_UPGRADED";
        }

        var potion = new ItemStack(material, 1);

        this.potionMeta.setBasePotionData(data);
        potion.setItemMeta(this.potionMeta);

        return new DefaultItem(name, potion);
    }
}
