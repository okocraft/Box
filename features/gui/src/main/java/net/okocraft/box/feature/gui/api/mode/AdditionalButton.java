package net.okocraft.box.feature.gui.api.mode;

import net.okocraft.box.feature.gui.api.button.Button;

public abstract class AdditionalButton implements Button {

    private int slot;

    @Override
    public final int getSlot() {
        return slot;
    }

    public final void setSlot(int slot) {
        this.slot = slot;
    }

}
