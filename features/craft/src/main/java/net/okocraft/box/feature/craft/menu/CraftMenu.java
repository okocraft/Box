package net.okocraft.box.feature.craft.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.craft.button.ChangeCraftTimesButton;
import net.okocraft.box.feature.craft.button.ChangeUnitButton;
import net.okocraft.box.feature.craft.button.CraftButton;
import net.okocraft.box.feature.craft.button.DistributionButton;
import net.okocraft.box.feature.craft.button.IngredientChangeModeButton;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.model2.BoxItemRecipe;
import net.okocraft.box.feature.craft.model2.IngredientHolder;
import net.okocraft.box.feature.craft.model2.SelectedRecipe;
import net.okocraft.box.feature.craft.util.CustomCraftTimes;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.menu.AbstractMenu;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.menu.RenderedButton;
import net.okocraft.box.feature.gui.internal.button.BackButton;
import net.okocraft.box.feature.gui.internal.button.CloseButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CraftMenu extends AbstractMenu {

    private static final List<Integer> RECIPE_SLOTS = List.of(10, 11, 12, 19, 20, 21, 28, 29, 30);
    private static final int[] CRAFT_TIMES = {1, 10, 64};

    private final BoxItemRecipe recipe;
    private final Menu backTo;

    private final CurrentRecipe currentRecipe;
    private final AtomicBoolean bulkIngredientChange = new AtomicBoolean(true);
    private final AtomicBoolean updateFlag = new AtomicBoolean(false);

    public CraftMenu(@NotNull BoxItemRecipe recipe, @Nullable Menu backTo) {
        this.recipe = recipe;
        this.backTo = backTo;
        this.currentRecipe = new CurrentRecipe(recipe);
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public @NotNull Component getTitle() {
        return Displays.CRAFT_MENU_TITLE.apply(recipe.result());
    }

    @Override
    public boolean shouldUpdate() {
        return updateFlag.getAndSet(false);
    }

    @Override
    public void updateMenu(@NotNull Player viewer) {
        var buttons = new ArrayList<Button>();

        for (int slot = 0, limit = getRows() * 9; slot < limit; slot++) {
            if (!RECIPE_SLOTS.contains(slot) && slot != 24) {
                buttons.add(Button.empty(Material.GRAY_STAINED_GLASS_PANE, slot));
            }
        }

        buttons.add(new ChangeCraftTimesButton(45, false, this));
        buttons.add(new ChangeUnitButton(46, this));
        buttons.add(new ChangeCraftTimesButton(47, true, this));
        buttons.add(backTo != null ? new BackButton(backTo, 49) : new CloseButton());

        buttons.add(
                new CraftButton(currentRecipe::getSelectedRecipe,
                        () -> CustomCraftTimes.getAmount(viewer), viewer, 48, updateFlag)
        );

        for (int i = 0; i < CRAFT_TIMES.length; i++) {
            buttons.add(new CraftButton(currentRecipe::getSelectedRecipe, CRAFT_TIMES[i], viewer, 50 + i, updateFlag));
        }

        buttons.add(new DistributionButton(viewer));

        for (var ingredient : recipe.ingredients()) {
            int pos = ingredient.getSlot();
            addButton(new RecipeItemIcon(currentRecipe, RECIPE_SLOTS.get(pos), pos, bulkIngredientChange, updateFlag));
        }

        var result = recipe.result().getClonedItem();
        result.setAmount(recipe.amount());
        addButton(new ResultItemIcon(result, 24));

        buttons.add(new IngredientChangeModeButton(bulkIngredientChange));

        buttons.stream().map(RenderedButton::create)
                .peek(button -> button.updateIcon(viewer))
                .forEach(this::addButton);
    }

    static class CurrentRecipe {

        private final BoxItemRecipe source;
        private final Map<Integer, IngredientHolder.SelectableIngredients> ingredientsMap;

        private SelectedRecipe selectedRecipe;

        private CurrentRecipe(@NotNull BoxItemRecipe source) {
            this.source = source;
            this.ingredientsMap =
                    source.ingredients().stream()
                            .filter(Predicate.not(ingredients -> ingredients.getPatterns().isEmpty()))
                            .collect(Collectors.toUnmodifiableMap(
                                    IngredientHolder::getSlot,
                                    IngredientHolder::toSelectableIngredients
                            ));

            updateSelectedRecipe();
        }

        void nextRecipe(int pos, boolean sameIngredient) {
            var recipe = ingredientsMap.get(pos);

            if (recipe == null) {
                return;
            }

            var selected = recipe.next();

            if (sameIngredient) {
                for (var other : ingredientsMap.values()) {
                    if (recipe != other && recipe.isSameIngredient(other)) {
                        other.select(selected);
                    }
                }
            }

            updateSelectedRecipe();
        }

        @Nullable IngredientHolder.SelectableIngredients getIngredients(int pos) {
            return ingredientsMap.get(pos);
        }

        private void updateSelectedRecipe() {
            this.selectedRecipe =
                    new SelectedRecipe(
                            ingredientsMap.values().stream()
                                    .map(IngredientHolder.SelectableIngredients::getSelected).toList(),
                            source.result(),
                            source.amount()
                    );
        }

        private @NotNull SelectedRecipe getSelectedRecipe() {
            return selectedRecipe;
        }
    }
}
