package net.okocraft.box.feature.autostore.event;

import net.okocraft.box.api.event.AsyncEvent;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when the {@link AutoStoreSetting} has changed.
 */
public class AutoStoreSettingChangeEvent extends BoxEvent implements AsyncEvent {

    private final AutoStoreSetting setting;

    /**
     * The constructor of the {@link AutoStoreSettingChangeEvent}.
     *
     * @param setting the changed {@link AutoStoreSetting}
     */
    public AutoStoreSettingChangeEvent(@NotNull AutoStoreSetting setting) {
        this.setting = setting;
    }

    /**
     * Gets the changed {@link AutoStoreSetting}.
     *
     * @return the changed {@link AutoStoreSetting}
     */
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
