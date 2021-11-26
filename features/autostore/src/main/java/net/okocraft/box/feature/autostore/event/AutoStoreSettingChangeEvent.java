package net.okocraft.box.feature.autostore.event;

import net.okocraft.box.api.event.AsyncEvent;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.jetbrains.annotations.NotNull;

public class AutoStoreSettingChangeEvent extends BoxEvent implements AsyncEvent {

    private final AutoStoreSetting setting;

    public AutoStoreSettingChangeEvent(@NotNull AutoStoreSetting setting) {
        this.setting = setting;
    }

    public @NotNull AutoStoreSetting getSetting() {
        return setting;
    }

    @Override
    public String toString() {
        return "AutoStoreSettingChangeEvent{" +
                "setting=" + setting +
                '}';
    }
}
