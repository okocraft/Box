package net.okocraft.box.feature.autostore.gui.buttons;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.button.Button;
import org.jetbrains.annotations.NotNull;

abstract class AbstractAutoStoreSettingButton implements Button {

    private final int slot;

    protected AbstractAutoStoreSettingButton(int slot) {
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    protected void callAutoStoreSettingChangeEvent(@NotNull AutoStoreSetting setting) {
        BoxProvider.get().getEventBus().callEventAsync(new AutoStoreSettingChangeEvent(setting));
    }
}
