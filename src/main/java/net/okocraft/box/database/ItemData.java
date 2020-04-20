package net.okocraft.box.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

/**
 * 以前のBoxのタブ補完の互換性を確保する実装。
 */
public class ItemData {

    private final ItemTable itemTable;

    private final BiMap<String, ItemStack> names = HashBiMap.create();

    public ItemData(PlayerData playerData) {
        this.itemTable = playerData.getItemTable();
        loadNames();
    }

    @Nullable
    public String getName(ItemStack item) {
        return names.inverse().get(item);
    }
    
    @Nullable
    public ItemStack getItemStack(String name) {
        return names.get(name.toUpperCase(Locale.ROOT));
    }

    public List<String> getNames() {
        return new ArrayList<>(names.keySet());
    }

    /**
     * loadNameを全てのアイテムについて行う。
     */
    private void loadNames() {
        itemTable.getAllItem().forEach(this::loadName);
    }

    /**
     * アイテムの、タブ補完などでやり取りする名前を全て読み込む。
     * 
     * @param id アイテムのデータベースに採番された番号
     * @param item アイテム
     */
    void loadName(ItemStack item) {
        item = item.clone();
        item.setAmount(1);
        int id = itemTable.getId(item);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof PotionMeta) {
            // FOR COMPATIBILITY.
            // names all creative items.
            PotionMeta potionMeta = (PotionMeta) meta;
            PotionData base = potionMeta.getBasePotionData();

            boolean extended = base.isExtended();
            boolean upgraded = base.isUpgraded();

            if (extended && upgraded) {
                base = new PotionData(base.getType(), !extended, upgraded);
                extended = !extended;
            }

            String extendedPart = extended ? "_EXTENDED" : "";
            String upgradedPart = upgraded ? "_UPGRADED" : "";
            String name = item.getType().name() + "_" + base.getType().name() + extendedPart + upgradedPart;

            potionMeta.setBasePotionData(new PotionData(PotionType.UNCRAFTABLE));
            ItemStack clone = item.clone();
            clone.setItemMeta(potionMeta);
            if (clone.hasItemMeta()) {
                name = name + ":" + id;
            }

            names.put(name, item);
            return;
        }

        if (!item.hasItemMeta()) {
            names.put(item.getType().name(), item);
        } else {
            names.put(item.getType().name() + ":" + id, item);
        }
    }

}