package net.okocraft.box.feature.autostore.gui.buttons;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import org.jetbrains.annotations.NotNull;

abstract class AbstractAutoStoreSettingButton implements RefreshableButton {

    private final int slot;
    protected final AutoStoreSetting setting;

    protected AbstractAutoStoreSettingButton(int slot, @NotNull AutoStoreSetting setting) {
        this.slot = slot;
        this.setting = setting;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    protected void callAutoStoreSettingChangeEvent() {
        BoxProvider.get().getEventBus().callEventAsync(new AutoStoreSettingChangeEvent(setting));
    }
}
