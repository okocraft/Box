package net.okocraft.box.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;
import net.okocraft.box.database.ItemData;

/**
 * クラフトGUIにおいて、デフォルトで適応されるレシピを上書きするために利用される。
 */
public final class CraftRecipes extends CustomConfig {

    private final Box plugin = Box.getInstance();

    /**
     * コンストラクタ。
     * @deprecated 内部利用限定。このコンストラクタを使わず、{@code Box.getInstance().getAPI().getCraftRecipes()}を使用すること。
     */
    @Deprecated
    public CraftRecipes() {
        super("craftrecipes.yml");
    }

    /**
     * {@code item}のクラフト時の材料を取得する。
     * {@code craftrecipes.yml}に指定されていなかったときは空の不変マップを返す。
     * 
     * @param item 作り方を取得するアイテム
     * @return アイテムの材料。取得できなければ空の不変マップ。
     */
    public Map<String, Integer> getIngredients(ItemStack item) {
        String itemName = getItemName(item);
        if (itemName == null) {
            return Map.of();
        }
        
        ConfigurationSection section = get().getConfigurationSection(itemName + ".ingredients");
        if (section == null) {
            return Map.of();
        }

        Map<String, Integer> ingredients = new HashMap<>();
        int ingredientAmount;
        for (String ingredient : section.getKeys(false)) {
            if (!section.isInt(ingredient) || (ingredientAmount = section.getInt(ingredient)) == 0) {
                continue;
            }

            if (getItemStack(ingredient) == null) {
                return Map.of();
            }

            ingredients.put(ingredient, ingredientAmount);            
        }

        return ingredients;
    }

    /**
     * {@code item}をクラフトした結果、いくつクラフトされるかを取得する。
     * データベースにアイテムが登録されていなかったり、{@code craftrecipes.yml}に指定されていなかった場合は0を返す。
     * 
     * @param item 一度の作成数を調べるアイテム
     * @return 一度の作成数、取得できなければ0。
     */
    public int getResultAmount(ItemStack item) {
        String itemName = getItemName(item);
        if (itemName == null) {
            return 0;
        }
        return get().getInt(itemName + ".result-amount");
    }

    private String getItemName(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemData itemData = plugin.getAPI().getItemData();
        return itemData.getName(item);
    }

    private ItemStack getItemStack(String name) {
        if (name == null) {
            return null;
        }

        return plugin.getAPI().getItemData().getItemStack(name);
    }
}
