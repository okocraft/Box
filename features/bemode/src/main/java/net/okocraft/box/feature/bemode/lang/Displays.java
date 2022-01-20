package net.okocraft.box.feature.bemode.lang;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
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

    public static final SingleArgument<Integer> STORAGE_MODE_CURRENT_STOCK =
            stock ->
                    translatable()
                            .key("box.bemode.storage-mode.current-stock")
                            .args(text(stock, AQUA))
                            .style(NO_DECORATION_GRAY)
                            .build();
    
    private Displays() {
        throw new UnsupportedOperationException();
    }
}
