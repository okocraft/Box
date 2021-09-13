package net.okocraft.box.core.config;

import com.github.siroshun09.configapi.api.value.ConfigValue;

import java.util.List;

public final class Settings {

    public static final ConfigValue<Boolean> DEBUG =
            config -> config.getBoolean("debug", false);

    public static final ConfigValue<Boolean> ITEM_ENABLE_DEFAULTS =
            config -> config.getBoolean("item.enable-defaults", true);

    public static final ConfigValue<Boolean> ITEM_ENABLE_POTIONS =
            config -> config.getBoolean("item.enable-potions", false);

    public static final ConfigValue<Boolean> ITEM_ENABLE_ENCHANTED_BOOKS =
            config -> config.getBoolean("item.enable-enchanted-books", false);

    public static final ConfigValue<Boolean> ITEM_ENABLE_FIREWORK_ROCKETS =
            config -> config.getBoolean("item.enable-firework-rockets", false);

    public static final ConfigValue<List<String>> DISABLED_WORLDS =
            config -> config.getStringList("disabled-worlds");

    private Settings() {
        throw new UnsupportedOperationException();
    }
}
