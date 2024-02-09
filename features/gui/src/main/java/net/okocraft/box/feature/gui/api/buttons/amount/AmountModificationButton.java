package net.okocraft.box.feature.gui.api.buttons.amount;

import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import org.jetbrains.annotations.NotNull;

abstract class AmountModificationButton implements Button {

    private final int slot;
    private final TypedKey<Amount> dataKey;

    protected AmountModificationButton(int slot, @NotNull TypedKey<Amount> dataKey) {
        this.slot = slot;
        this.dataKey = dataKey;
    }

    @Override
    public final int getSlot() {
        return slot;
    }

    protected final @NotNull Amount getOrCreateAmount(@NotNull PlayerSession session) {
        return session.computeDataIfAbsent(this.dataKey, Amount::new);
    }
}
