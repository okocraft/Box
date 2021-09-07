package net.okocraft.box.autostore.model.mode;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

public class AllModeSetting implements AutoStoreMode {

    private boolean enabled;

    @Override
    public @NotNull String getModeName() {
        return "all";
    }

    @Override
    public boolean isEnabled(@NotNull BoxItem item) {
        return isEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean toggleEnabled() {
        boolean toggled = !isEnabled();

        setEnabled(toggled);

        return toggled;
    }

    @Override
    public String toString() {
        return "AllModeSetting{" +
                "enabled=" + enabled +
                '}';
    }
}
