package net.okocraft.box.api.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Box で設定・使用可能なレシピのインターフェース
 */
public interface CustomRecipe extends Recipe {

    /**
     * このレシピに必要なアイテムを返す。
     *
     * @return 必要なアイテム
     */
    @NotNull @Unmodifiable List<ItemStack> getIngredients();
}
