package net.okocraft.box.feature.craft.gui.menu;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.craft.gui.button.CraftButton;
import net.okocraft.box.feature.craft.gui.button.IngredientButton;
import net.okocraft.box.feature.craft.gui.button.IngredientChangeModeButton;
import net.okocraft.box.feature.craft.gui.button.ResultButton;
import net.okocraft.box.feature.craft.gui.button.ToggleDestinationButton;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.buttons.BackOrCloseButton;
import net.okocraft.box.feature.gui.api.buttons.amount.DecreaseAmountButton;
import net.okocraft.box.feature.gui.api.buttons.amount.IncreaseCustomNumberButton;
import net.okocraft.box.feature.gui.api.buttons.amount.UnitChangeButton;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CraftMenu implements Menu {

    private static final TypedKey<Amount> KEY = TypedKey.of(Amount.class, "craft-times");

    private static final List<Button> SHARED_BUTTONS;

    static {
        var buttons = new ArrayList<Button>(54);
        var recipeSlots = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};

        var reservedSlots = new IntOpenHashSet(recipeSlots);

        reservedSlots.add(18);
        reservedSlots.add(24);

        for (int slot = 0; slot < 45; slot++) {
            if (!reservedSlots.contains(slot)) {
                buttons.add(Button.glassPane(slot));
            }
        }

        for (int i = 0; i < recipeSlots.length; i++) {
            buttons.add(new IngredientButton(recipeSlots[i], i));
        }

        buttons.add(new IngredientChangeModeButton(18));
        buttons.add(new ResultButton(24));

        buttons.add(
                new DecreaseAmountButton(
                        45,
                        KEY,
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_DECREASE_DISPLAY_NAME,
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_DECREASE_LORE,
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_CURRENT,
                        ClickResult.UPDATE_ICONS
                )
        );

        buttons.add(
                new UnitChangeButton(
                        46,
                        KEY,
                        Displays.CHANGE_UNIT_BUTTON_DISPLAY_NAME,
                        Displays.CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_TIMES,
                        ClickResult.UPDATE_ICONS
                )
        );

        buttons.add(
                new IncreaseCustomNumberButton(
                        47,
                        KEY,
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_INCREASE_DISPLAY_NAME,
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_SET_TO_UNIT,
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_INCREASE_LORE,
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_CURRENT,
                        ClickResult.UPDATE_ICONS
                )
        );

        buttons.add(new CraftButton(48, KEY));
        buttons.add(new BackOrCloseButton(49));

        buttons.add(new CraftButton(50, 1));
        buttons.add(new CraftButton(51, 10));
        buttons.add(new CraftButton(52, 64));

        buttons.add(new ToggleDestinationButton(53));

        SHARED_BUTTONS = Collections.unmodifiableList(buttons);
    }

    private static final CraftMenu INSTANCE = new CraftMenu();

    public static @NotNull CraftMenu prepare(@NotNull PlayerSession session, @NotNull BoxItemRecipe recipe) {
        session.putData(CurrentRecipe.DATA_KEY, new CurrentRecipe(recipe));
        return INSTANCE;
    }

    private CraftMenu() {
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        var recipe = session.getDataOrThrow(CurrentRecipe.DATA_KEY);
        return Displays.CRAFT_MENU_TITLE.apply(recipe.getResult());
    }

    @Override
    public @NotNull List<? extends Button> getButtons(@NotNull PlayerSession session) {
        return SHARED_BUTTONS;
    }
}
