package net.okocraft.box.plugin.model.item;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.manager.ItemManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BoxRecipe implements Recipe {

    private final Item result;
    private final int resultAmount;
    private final Map<Item, Integer> ingredients;

    public BoxRecipe(@NotNull Item result, int resultAmount, @NotNull Map<Item, Integer> ingredients) {
        this.result = result;
        this.resultAmount = resultAmount;
        this.ingredients = ingredients;
    }

    @Override
    @NotNull
    public ItemStack getResult() {
        ItemStack stack = result.getOriginalCopy();
        stack.setAmount(resultAmount);
        return stack;
    }

    /**
     * クラフト結果のアイテムの数を取得できない。
     * その数を取得するときは{@link net.okocraft.box.plugin.model.item.BoxRecipe#getResult()}
     * で得られるItemStackのamountを取得すれば良い。
     * 
     * @return Itemクラスで表されるクラフト結果のアイテム
     */
    @NotNull
    public Item getResultAsItem() {
        return result;
    }

    @NotNull
    public Map<Item, Integer> getIngredients() {
        return ingredients;
    }

    @Nullable
    public static BoxRecipe of(Item item) {
        return of(item.getOriginalCopy());
    }

    @Nullable
    public static BoxRecipe of(ItemStack item) {
        List<Recipe> recipes = Bukkit.getRecipesFor(item);
        recipes.removeIf(recipe -> !(recipe instanceof ShapelessRecipe || recipe instanceof ShapedRecipe));
        if (recipes.isEmpty()) {
            return null;
        }

        return asBoxRecipe(recipes.get(0));
    }

    @Nullable
    public static BoxRecipe asBoxRecipe(@NotNull Recipe bukkitRecipe) throws IllegalArgumentException {
        ItemManager im = JavaPlugin.getPlugin(Box.class).getItemManager();

        ItemStack resultItemStack = bukkitRecipe.getResult();
        setDamage(resultItemStack, (short) 0);
        Optional<Item> resultItem = im.getItem(resultItemStack);
        if (resultItem.isEmpty()) {
            return null;
        }

        List<ItemStack> ingredients;
        if (bukkitRecipe instanceof ShapelessRecipe) {
            ingredients = new ArrayList<>(((ShapelessRecipe) bukkitRecipe).getIngredientList());
        } else if (bukkitRecipe instanceof ShapedRecipe) {
            ingredients = new ArrayList<>(((ShapedRecipe) bukkitRecipe).getIngredientMap().values());
        } else {
            throw new IllegalArgumentException("Recipe must be ShapelessRecipe or ShapedRecipe.");
        }
        ingredients.removeIf(Objects::isNull);

        Map<Item, Integer> result = new HashMap<>();
        for (ItemStack ingredient : ingredients) {
            setDamage(ingredient, (short) 0);
            Optional<Item> optionalIngredient = im.getItem(ingredient);
            if (optionalIngredient.isEmpty()) {
                return null;
            }
            result.put(
                optionalIngredient.get(),
                result.getOrDefault(optionalIngredient.get(), 0) + ingredient.getAmount()
            );
        }

        return new BoxRecipe(resultItem.get(), resultItemStack.getAmount(), result);
    }

    private static void setDamage(ItemStack item, short damage) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta instanceof Damageable) {
            ((Damageable) meta).setDamage(damage);
            item.setItemMeta(meta);
        }
    }
}
