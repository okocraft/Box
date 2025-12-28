package net.okocraft.box.feature.craft.gui.menu;

import dev.siroshun.mcmsgdef.MessageKey;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.gui.CurrentRecipe;
import net.okocraft.box.feature.craft.gui.button.CraftButton;
import net.okocraft.box.feature.craft.gui.button.IngredientButton;
import net.okocraft.box.feature.craft.gui.button.IngredientChangeModeButton;
import net.okocraft.box.feature.craft.gui.button.IngredientOrderButton;
import net.okocraft.box.feature.craft.gui.button.ResultButton;
import net.okocraft.box.feature.craft.gui.button.ToggleDestinationButton;
import net.okocraft.box.feature.craft.lang.CraftPlaceholders;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.model.BoxIngredientItem;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CraftMenu implements Menu {

    private static final TypedKey<Amount> KEY = TypedKey.of(Amount.class, "craft-times");
    public static final TypedKey<IngredientOrder> INGREDIENT_ORDER_KEY = TypedKey.of(IngredientOrder.class, "ingredient-order");

    private static final MessageKey.Arg1<BoxItem> TITLE = MessageKey.arg1(DisplayKeys.CRAFT_MENU_TITLE, Placeholders.ITEM);
    private static final List<Button> SHARED_BUTTONS;

    static {
        var buttons = new ArrayList<Button>(54);
        var recipeSlots = new int[]{10, 11, 12, 19, 20, 21, 28, 29, 30};

        var reservedSlots = new IntOpenHashSet(recipeSlots);

        reservedSlots.add(9);
        reservedSlots.add(24);
        reservedSlots.add(27);

        for (int slot = 0; slot < 45; slot++) {
            if (!reservedSlots.contains(slot)) {
                buttons.add(Button.glassPane(slot));
            }
        }

        for (int i = 0; i < recipeSlots.length; i++) {
            buttons.add(new IngredientButton(recipeSlots[i], i));
        }

        buttons.add(new IngredientChangeModeButton(9));
        buttons.add(new ResultButton(24));
        buttons.add(new IngredientOrderButton(27));

        buttons.add(
            new DecreaseAmountButton(
                45,
                KEY,
                MessageKey.key(DisplayKeys.DECREASE_CRAFT_TIMES_DISPLAY_NAME),
                MessageKey.arg1(DisplayKeys.DECREASE_CRAFT_TIMES_LORE, CraftPlaceholders.TIMES),
                MessageKey.arg1(DisplayKeys.CURRENT_CRAFT_TIMES, CraftPlaceholders.TIMES),
                ClickResult.UPDATE_ICONS
            )
        );

        buttons.add(
            new UnitChangeButton(
                46,
                KEY,
                MessageKey.key(DisplayKeys.CHANGE_UNIT),
                MessageKey.key(DisplayKeys.RESET_CRAFT_TIMES),
                ClickResult.UPDATE_ICONS
            )
        );

        buttons.add(
            new IncreaseAmountButton(
                47,
                KEY,
                MessageKey.key(DisplayKeys.INCREASE_CRAFT_TIMES_DISPLAY_NAME),
                MessageKey.arg1(DisplayKeys.SET_CRAFT_TIMES_LORE, CraftPlaceholders.TIMES),
                MessageKey.arg1(DisplayKeys.INCREASE_CRAFT_TIMES_LORE, CraftPlaceholders.TIMES),
                MessageKey.arg1(DisplayKeys.CURRENT_CRAFT_TIMES, CraftPlaceholders.TIMES),
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

        var order = session.getData(INGREDIENT_ORDER_KEY);
        this.currentRecipe.sortIngredients(order != null ? order.createSorter(session) : null);
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public @NotNull Component getTitle(@NotNull PlayerSession session) {
        return TITLE.apply(session.getDataOrThrow(CurrentRecipe.DATA_KEY).getResult()).asComponent();
    }

    @Override
    public @NotNull List<? extends Button> getButtons(@NotNull PlayerSession session) {
        return SHARED_BUTTONS;
    }

    public interface IngredientOrder {

        IngredientOrder NORMAL = ignored -> null;
        IngredientOrder STOCK_AMOUNT = session -> Comparator.<BoxIngredientItem>comparingInt(ingredient -> session.getSourceStockHolder().getAmount(ingredient.item())).reversed();

        @Nullable Comparator<BoxIngredientItem> createSorter(@NotNull PlayerSession session);
    }
}
