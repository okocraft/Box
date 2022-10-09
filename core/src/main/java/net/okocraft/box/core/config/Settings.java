package net.okocraft.box.core.config;

import com.github.siroshun09.configapi.api.value.ConfigValue;

import java.util.List;

public final class Settings {

    public static final ConfigValue<Boolean> DEBUG =
            config -> config.getBoolean("debug", false);

    public static final ConfigValue<List<String>> DISABLED_WORLDS =
            config -> config.getStringList("disabled-worlds");

    public static final ConfigValue<List<String>> DISABLED_FEATURES =
            config -> config.getStringList("disabled-features");

    public static final ConfigValue<Long> STOCK_DATA_SAVE_INTERVAL =
            config -> config.getLong("stock-data-save-interval", 5);

    public static final ConfigValue<Boolean> ALLOW_MINUS_STOCK =
            config -> config.getBoolean("allow-minus-stock", false);

    private Settings() {
        throw new UnsupportedOperationException();
    }
}
