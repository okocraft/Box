package net.okocraft.box.feature.craft.model;

import net.okocraft.box.feature.craft.menu.ResultItemIcon;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record RecipePreview(@NotNull List<ItemStack> ingredients, @NotNull ItemStack resultItem) {

    public @NotNull List<ResultItemIcon> getIngredientIcons(int startSlot) {
        var result = new ArrayList<ResultItemIcon>(9);

        int column = 0;
        int row = 0;
        for (var ingredient : ingredients) {
            result.add(new ResultItemIcon(ingredient, startSlot + (column + row * 9)));

            column++;

            if (2 < column) {
                column = 0;
                row++;
            }
        }

        return result;
    }

    public @NotNull ResultItemIcon getResultItemIcon(int slot) {
        return new ResultItemIcon(resultItem, slot);
    }
}
