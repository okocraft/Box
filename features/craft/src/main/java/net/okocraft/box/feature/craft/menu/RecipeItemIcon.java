package net.okocraft.box.feature.craft.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.feature.gui.api.menu.RenderedButton;
import net.okocraft.box.feature.gui.internal.lang.Styles;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public record RecipeItemIcon(@NotNull CraftMenu.CurrentRecipe currentRecipe, int slot, int pos,
                             @NotNull AtomicBoolean changeSameIngredientFlag,
                             @NotNull AtomicBoolean updateFlag) implements RenderedButton {

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        var ingredients = currentRecipe.getIngredients(pos);

        if (ingredients == null) {
            return new ItemStack(Material.AIR);
        }

        var selected = ingredients.getSelected();
        var icon = selected.item().getClonedItem();
        icon.setAmount(selected.amount());

        if (ingredients.size() == 1) {
            return icon;
        }

        var meta = icon.getItemMeta();

        if (meta == null) {
            return icon;
        }

        var lore = new ArrayList<Component>();

        for (var ingredient : ingredients.get()) {
            lore.add(
                    Component.text()
                            .append(Component.text(" > "))
                            .append(Component.translatable(ingredient.item().getOriginal()))
                            .style(Styles.NO_STYLE)
                            .color(ingredient == ingredients.getSelected() ? NamedTextColor.AQUA : NamedTextColor.GRAY)
                            .build()
            );
        }

        meta.lore(lore);
        icon.setItemMeta(meta);

        return icon;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }

    @Override
    public void updateIcon(@NotNull Player viewer) {
    }

    @Override
    public void clickButton(@NotNull Player clicker, @NotNull ClickType clickType) {
        currentRecipe.nextRecipe(pos, changeSameIngredientFlag.get());
        updateFlag.set(true);
    }
}
