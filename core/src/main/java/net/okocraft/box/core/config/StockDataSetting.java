package net.okocraft.box.core.config;

import dev.siroshun.configapi.core.serialization.annotation.Comment;
import dev.siroshun.configapi.core.serialization.annotation.DefaultLong;
import net.okocraft.box.api.util.BoxLogger;
import org.jetbrains.annotations.NotNull;

public record StockDataSetting(
    @Comment("Number of seconds to unload the player's stock data after logging out.")
    @DefaultLong(300)
    long unloadTime,
    @Comment("Interval in seconds to save player stock data.")
    @DefaultLong(15)
    long saveInterval
) {

    @Override
    public long unloadTime() {
        if (this.unloadTime < 1) {
            logInvalidValue("unload-time", this.unloadTime, 300);
            return 300;
        }
        return this.unloadTime;
    }

    @Override
    public long saveInterval() {
        if (this.saveInterval < 1) {
            logInvalidValue("save-interval", this.saveInterval, 15);
            return 15;
        }
        return this.saveInterval;
    }

    private static void logInvalidValue(@NotNull String key, long invalidValue, long defaultValue) {
        BoxLogger.logger().warn("{} is invalid value ({}), so using default value ({})...", key, invalidValue, defaultValue);
    }
}
