package net.okocraft.box.feature.craft.gui.menu;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.gui.button.CraftButton;
import net.okocraft.box.feature.craft.gui.button.IngredientButton;
import net.okocraft.box.feature.craft.gui.button.IngredientChangeModeButton;
import net.okocraft.box.feature.craft.gui.button.ResultButton;
import net.okocraft.box.feature.craft.gui.button.ToggleDestinationButton;
import net.okocraft.box.feature.craft.lang.CraftPlaceholders;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.model.BoxItemRecipe;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.buttons.BackOrCloseButton;
import net.okocraft.box.feature.gui.api.buttons.amount.DecreaseAmountButton;
import net.okocraft.box.feature.gui.api.buttons.amount.IncreaseAmountButton;
import net.okocraft.box.feature.gui.api.buttons.amount.UnitChangeButton;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class CraftMenu implements Menu {

    private static final TypedKey<Amount> KEY = TypedKey.of(Amount.class, "craft-times");

    private static final Arg1<BoxItem> TITLE = arg1(DisplayKeys.CRAFT_MENU_TITLE, Placeholders.ITEM);
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
                        messageKey(DisplayKeys.DECREASE_CRAFT_TIMES_DISPLAY_NAME),
                        arg1(DisplayKeys.DECREASE_CRAFT_TIMES_LORE, CraftPlaceholders.TIMES),
                        arg1(DisplayKeys.CURRENT_CRAFT_TIMES, CraftPlaceholders.TIMES),
                        ClickResult.UPDATE_ICONS
                )
        );

        buttons.add(
                new UnitChangeButton(
                        46,
                        KEY,
                        messageKey(DisplayKeys.CHANGE_UNIT),
                        messageKey(DisplayKeys.RESET_CRAFT_TIMES),
                        ClickResult.UPDATE_ICONS
                )
        );

        buttons.add(
                new IncreaseAmountButton(
                        47,
                        KEY,
                        messageKey(DisplayKeys.INCREASE_CRAFT_TIMES_DISPLAY_NAME),
                        arg1(DisplayKeys.SET_CRAFT_TIMES_LORE, CraftPlaceholders.TIMES),
                        arg1(DisplayKeys.INCREASE_CRAFT_TIMES_LORE, CraftPlaceholders.TIMES),
                        arg1(DisplayKeys.CURRENT_CRAFT_TIMES, CraftPlaceholders.TIMES),
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

    private final CurrentRecipe currentRecipe;

    public static @NotNull CraftMenu prepare(@NotNull BoxItemRecipe recipe) {
        return new CraftMenu(new CurrentRecipe(recipe));
    }

    private CraftMenu(@NotNull CurrentRecipe currentRecipe) {
        this.currentRecipe = currentRecipe;
    }

    @Override
    public void onOpen(@NotNull PlayerSession session) {
        session.putData(CurrentRecipe.DATA_KEY, this.currentRecipe);
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        return TITLE.apply(session.getDataOrThrow(CurrentRecipe.DATA_KEY).getResult()).create(session.getMessageSource());
    }

    @Override
    public @NotNull List<? extends Button> getButtons(@NotNull PlayerSession session) {
        return SHARED_BUTTONS;
    }
}
