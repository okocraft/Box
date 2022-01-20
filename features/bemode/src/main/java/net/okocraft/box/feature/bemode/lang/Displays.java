package net.okocraft.box.feature.bemode.lang;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GOLD;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;

public final class Displays {

    public static final Component STORAGE_DEPOSIT_MODE_DISPLAY_NAME =
            translatable("box.bemode.storage-deposit-mode.display-name");

    public static final Component STORAGE_WITHDRAW_MODE_DISPLAY_NAME =
            translatable("box.bemode.storage-withdraw-mode.display-name");


    public static final SingleArgument<Integer> STORAGE_DEPOSIT_MODE_CLICK_TO_DEPOSIT =
            transactionUnit ->
                    translatable()
                            .key("box.bemode.storage-deposit-mode.click-to-deposit")
                            .args(text(transactionUnit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> STORAGE_WITHDRAW_MODE_CLICK_TO_WITHDRAW =
            transactionUnit ->
                    translatable()
                            .key("box.bemode.storage-withdraw-mode.click-to-withdraw")
                            .args(text(transactionUnit, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final SingleArgument<Integer> CURRENT_STOCK =
            stock ->
                    translatable()
                            .key("box.bemode.current-stock")
                            .args(text(stock, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();

    public static final Component DEPOSIT_ALL_BUTTON_DISPLAY_NAME =
            translatable("box.bemode.deposit-all.display-name", NO_DECORATION_GOLD);

    public static final Component DEPOSIT_ALL_BUTTON_LORE =
            translatable("box.bemode.deposit-all.lore", NO_DECORATION_GRAY);

    public static final Component DEPOSIT_ALL_BUTTON_CONFIRMATION =
            translatable("box.bemode.deposit-all.confirmation", NO_DECORATION_GRAY);


    private Displays() {
        throw new UnsupportedOperationException();
    }
}
