package net.okocraft.box.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

/**
 * 以前のBoxのタブ補完の互換性を確保する実装。
 * アイテムの内部名、その内部名から実際のアイテムスタックを取得できる
 */
public class ItemData {

    private final ItemTable itemTable;

    private final BiMap<String, ItemStack> names = HashBiMap.create();

    /**
     * コンストラクタ。
     * 
     * @param playerData プレイヤーデータ
     * 
     * @deprecated 内部利用限定。このコンストラクタを使わず、{@code Box.getInstance().getAPI().getItemData()}を使用すること。
     */
    @Deprecated
    public ItemData(PlayerData playerData) {
        this.itemTable = playerData.getItemTable();
        loadNames();
    }

    /**
     * アイテムから内部名を取得する。内部名は{@code Material名:id}となっており、idはデータベースに登録された時に振り分けられた番号である。
     * もしアイテムに{@link ItemData#setCustomName(ItemStack, String)}によってカスタムネームが設定されていたら、それを返す。
     * データベースにアイテムがない場合は、nullを返す。
     * 
     * @param item 名前を調べるアイテム。
     * @return アイテム名またはnull
     */
    @Nullable
    public String getName(ItemStack item) {
        item = item.clone();
        item.setAmount(1);
        return names.inverse().get(item);
    }
    
    /**
     * アイテム名から{@link ItemStack}を調べて返す。
     * もしアイテムに{@link ItemData#setCustomName(ItemStack, String)}によってカスタムネームが設定されていたら、それを引数に渡さなければnullを返す。
     * アイテムがデータベースにない場合もnullを返す。
     * 
     * @param name アイテム名
     * @return アイテムまたはnull
     */
    @Nullable
    public ItemStack getItemStack(String name) {
        ItemStack item = names.get(name.toUpperCase(Locale.ROOT));
        return item != null ? item.clone() : null;
    }

    /**
     * アイテムをデータベースに登録する。その時に設定されたアイテム名を返す。もし、登録できなかった場合は空文字列を返す。
     * 
     * @param item データベースに登録するアイテム
     * @return アイテム名または空文字列
     */
    public String register(ItemStack item) {
        String itemName = getName(item);
        if (itemName == null) {
            itemTable.register(item);
        }
        return Objects.requireNonNull(loadName(item), "");
    }

    /**
     * データベースに登録されているアイテムのすべての名前をリストに入れて返す。
     * 
     * @return アイテム名のリスト
     */
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
     * アイテムのカスタムネームを設定する。アイテムがデータベースに登録されて居ない場合は失敗する。
     * 
     * @param item アイテム
     * @param customName 新しいカスタムネーム
     * @return 成功したらtrue、さもなくばfalse
     */
    public boolean setCustomName(ItemStack item, String customName) {
        item = item.clone();
        item.setAmount(1);
        if (itemTable.setCustomName(item, customName)) {
            names.forcePut(customName, item);
            return true;
        }

        return false;
    }

    /**
     * アイテムの、タブ補完などでやり取りする名前を全て読み込む。
     * 
     * @param id アイテムのデータベースに採番された番号
     * @param item アイテム
     */
    String loadName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) {
            return null;
        }

        int id = itemTable.getId(item);
        if (id == -1) {
            return null;
        }

        item = item.clone();
        item.setAmount(1);

        String customName = itemTable.getCustomName(item);
        if (customName != null) {
            names.forcePut(customName, item);
            return customName;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof PotionMeta) {
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
                name = item.getType().name() + ":" + id;
            } else {
                setCustomName(item, name);
            }
            names.put(name, item);
            return name;
        }

        if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta enchantsMeta = (EnchantmentStorageMeta) item.getItemMeta();
            Map<Enchantment, Integer> enchants = enchantsMeta.getStoredEnchants();
            if (enchants.size() == 1) {
                Enchantment enchant = enchants.keySet().iterator().next();
                if (enchants.get(enchant) == enchant.getMaxLevel()) {
                    ItemStack someItem = new ItemStack(Material.STONE);
                    someItem.setItemMeta(enchantsMeta);
                    String name = item.getType().name();
                    if (someItem.hasItemMeta()) {
                        name = name + ":" + id;
                    } else {
                        name = name + "_" + enchant.getKey().getKey().toUpperCase(Locale.ROOT);
                        setCustomName(item, name);
                    }
                    names.put(name, item);
                    return name;
                }
            }
        }

        String name;
        if (!item.hasItemMeta()) {
            name = item.getType().name();
        } else {
            name = item.getType().name() + ":" + id;
        }
        names.put(name, item);
        return name;
    }

}