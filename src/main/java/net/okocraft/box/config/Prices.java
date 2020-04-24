package net.okocraft.box.config;

import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;

/**
 * 販売価格と買取価格の設定を取得できる。
 */
public final class Prices extends CustomConfig {

    /**
     * コンストラクタ。
     * @deprecated 内部利用限定。このコンストラクタを使わず、{@code Box.getInstance().getAPI().getPrices()}を使用すること。
     */
    @Deprecated
    public Prices() {
        super("prices.yml");
    }

    /**
     * 渡したアイテムの買取価格を取得する。{@code prices.yml}に記載されていないときは0を返す。
     * 
     * @param item アイテム
     * @return 買取価格。または0
     */
    public double getSellPrice(ItemStack item) {
        String name = Box.getInstance().getAPI().getItemData().getName(item);
        return get().getDouble(name + ".sell");
    }

    /**
     * 渡したアイテムの販売価格を取得する。{@code prices.yml}に記載されていないときは0を返す。
     * 
     * @param item アイテム
     * @return 販売価格。または0
     */
    public double getBuyPrice(ItemStack item) {
        String name = Box.getInstance().getAPI().getItemData().getName(item);
        return get().getDouble(name + ".buy");
    }
}