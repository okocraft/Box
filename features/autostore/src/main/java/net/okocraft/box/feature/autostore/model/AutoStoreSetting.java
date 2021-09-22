package net.okocraft.box.feature.autostore.model;

import net.okocraft.box.feature.autostore.model.mode.AllModeSetting;
import net.okocraft.box.feature.autostore.model.mode.AutoStoreMode;
import net.okocraft.box.feature.autostore.model.mode.PerItemModeSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AutoStoreSetting {

    private final AllModeSetting allModeSetting = new AllModeSetting();
    private final PerItemModeSetting perItemModeSetting = new PerItemModeSetting();
    private final Player player;

    private boolean enabled = false;

    private AutoStoreMode currentMode = allModeSetting;

    public AutoStoreSetting(@NotNull Player player) {
        this.player = player;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull AutoStoreMode getCurrentMode() {
        return currentMode;
    }

    public void setMode(@NotNull AutoStoreMode mode) {
        this.currentMode = mode;
    }

    public @NotNull AllModeSetting getAllModeSetting() {
        return allModeSetting;
    }

    public @NotNull PerItemModeSetting getPerItemModeSetting() {
        return perItemModeSetting;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoStoreSetting that = (AutoStoreSetting) o;
        return allModeSetting.equals(that.allModeSetting) &&
                perItemModeSetting.equals(that.perItemModeSetting) &&
                player.equals(that.player) &&
                currentMode.equals(that.currentMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allModeSetting, perItemModeSetting, player, currentMode);
    }

    @Override
    public String toString() {
        return "AutoStoreSetting{" +
                "allModeSetting=" + allModeSetting +
                ", perItemModeSetting=" + perItemModeSetting +
                ", player=" + player +
                ", enabled=" + enabled +
                ", currentMode=" + currentMode +
                '}';
    }
}
