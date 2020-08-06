package net.okocraft.box.plugin.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * {@link ItemStack} を構築するクラス。
 */
public final class ItemBuilder {

    private Material material;
    private String displayName;
    private List<String> lore;
    private boolean isGlowing;
    private int amount;

    /**
     * アイテムの {@link Material} をセットする。
     * <p>
     * このメソッドを実行しない、または {@code null} を渡した状態で {@link ItemBuilder#build()} を呼び出すと
     * {@link NullPointerException} がスルーされる。
     * <p>
     * {@link Material} の種類によっては、他のメソッドで設定した値が正常に適用されない場合がある。
     *
     * @param material アイテムの {@link Material}
     * @return このオブジェクトへの参照
     */
    public ItemBuilder setMaterial(Material material) {
        this.material = material;

        return this;
    }

    /**
     * アイテムの表示名をセットする。
     * <p>
     * このメソッドを実行しない、または {@code null} 渡した状態で {@link ItemBuilder#build()} を呼び出しても
     * そのまま {@link ItemStack} を構築が続行される。表示名はデフォルトのアイテム名となる (言語によって異なる)。
     *
     * @param displayName アイテムの表示名
     * @return このオブジェクトへの参照
     */
    public ItemBuilder setDisplayName(String displayName) {
        this.displayName = displayName;

        return this;
    }

    /**
     * アイテムの Lore をセットする。
     * <p>
     * このメソッドを実行しない、または {@code null} 渡した状態で {@link ItemBuilder#build()} を呼び出しても
     * そのまま {@link ItemStack} を構築が続行される。Lore は設定されない。
     *
     * @param lore アイテムの Lore
     * @return このオブジェクトへの参照
     */
    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;

        return this;
    }

    /**
     * アイテムが光っているかをセットする。
     * <p>
     * このメソッドを実行しない、または {@code null} 渡した状態で {@link ItemBuilder#build()} を呼び出しても
     * そのまま {@link ItemStack} を構築が続行される。アイテムは光らない。
     * <p>
     * 光らせる場合、内部的には {@link Enchantment#LURE} Lv.1 をセットし、エンチャントを非表示にする。
     * {@link Material#FISHING_ROD} 以外はこのエンチャントは効果を発揮しない。
     *
     * @param glowing アイテムを光らせるなら {@code true}, そうでなければ {@code false}
     * @return このオブジェクトへの参照
     */
    public ItemBuilder setGlowing(boolean glowing) {
        isGlowing = glowing;

        return this;
    }

    /**
     * アイテムの個数をセットする。
     * <p>
     * このメソッドを実行しない、または 0 以下を渡した状態で {@link ItemBuilder#build()} を呼び出しても
     * そのまま {@link ItemStack} を構築が続行される。アイテムの個数は 1 となる。
     *
     * @param amount アイテムの個数
     * @return このオブジェクトへの参照
     */
    public ItemBuilder setAmount(int amount) {
        this.amount = amount;

        return this;
    }

    /**
     * アイテムを構築する。
     *
     * @return 構築したアイテム
     * @throws NullPointerException {@link ItemBuilder#setMaterial(Material)} が実行されていないか、 {@code null} が渡された状態である場合。
     */
    @NotNull
    public ItemStack build() {
        Objects.requireNonNull(material);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);

            if (isGlowing) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }

        item.setItemMeta(meta);
        item.setAmount(0 < amount ? amount : 1);

        return item;
    }
}
