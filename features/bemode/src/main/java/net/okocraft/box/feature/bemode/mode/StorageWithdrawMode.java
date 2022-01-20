package net.okocraft.box.feature.bemode.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.bemode.lang.Displays;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import org.jetbrains.annotations.NotNull;

public class StorageWithdrawMode extends AbstractStorageMode {

    @Override
    public @NotNull String getName() {
        return "storage-withdraw";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.STORAGE_WITHDRAW_MODE_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        ClickModeRegistry.getStorageMode().processWithdraw(context);
    }

    @Override
    protected @NotNull Component getButtonInformationLore(@NotNull PlayerSession session) {
        int transactionAmount = session.getCustomNumberHolder(TRANSACTION_AMOUNT_NAME).getAmount();
        return Displays.STORAGE_WITHDRAW_MODE_CLICK_TO_WITHDRAW.apply(transactionAmount);
    }
}
