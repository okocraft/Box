package net.okocraft.box.feature.autostore.gui.buttons;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
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
        BoxAPI.api().getEventManager().callAsync(new AutoStoreSettingChangeEvent(setting));
    }
}
