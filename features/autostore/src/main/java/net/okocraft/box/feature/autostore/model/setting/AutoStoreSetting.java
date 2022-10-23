package net.okocraft.box.feature.autostore.model.setting;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AutoStoreSetting {

    private final UUID uuid;
    private final PerItemSetting perItemSetting = new PerItemSetting();
    private boolean enabled = false;
    private boolean allMode = true;
    private boolean direct = false;

    public AutoStoreSetting(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public @NotNull UUID getUuid() {
        return uuid;
    }

    public @NotNull PerItemSetting getPerItemModeSetting() {
        return perItemSetting;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAllMode() {
        return allMode;
    }

    public void setAllMode(boolean allMode) {
        this.allMode = allMode;
    }

    public boolean isDirect() {
        return this.direct;
    }

    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    public boolean shouldAutoStore(@NotNull BoxItem item) {
        return allMode || perItemSetting.isEnabled(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoStoreSetting that = (AutoStoreSetting) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "AutoStoreSetting{" +
                ", uuid=" + uuid +
                ", enabled=" + enabled +
                ", allMode=" + allMode +
                ", direct=" + direct +
                ", perItemModeSetting=" + perItemSetting +
                '}';
    }
}
