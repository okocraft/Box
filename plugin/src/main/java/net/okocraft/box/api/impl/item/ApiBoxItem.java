package net.okocraft.box.api.impl.item;

import net.okocraft.box.Box;
import net.okocraft.box.api.item.BoxItem;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

public class ApiBoxItem implements BoxItem {

    private final ItemStack item;

    public ApiBoxItem(@NotNull ItemStack item) {
        this.item = item;
    }

    @Override
    public @NotNull String getItemName() { // TODO: 一時的なコード。
        return Objects.requireNonNullElseGet(Box.getInstance().getAPI().getItemData().getName(item), () -> {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                return meta.getDisplayName();
            } else {
                return item.getType().toString();
            }
        });
    }

    @Override
    public @NotNull String getCustomName() { // TODO: 一時的なコード。
        return getItemName();
    }

    @Override
    public @NotNull ItemStack toItemStack() {
        return item;
    }

    @Override
    public @NotNull @Unmodifiable List<Recipe> getRecipes() {
        return List.copyOf(Bukkit.getRecipesFor(item));
    }

    @Override
    public double getSellingPrice() {
        return Box.getInstance().getAPI().getPrices().getSellPrice(item);
    }

    @Override
    public double getBuyingPrice() {
        return Box.getInstance().getAPI().getPrices().getBuyPrice(item);
    }
}
