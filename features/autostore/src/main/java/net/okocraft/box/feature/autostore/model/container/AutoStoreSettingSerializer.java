package net.okocraft.box.feature.autostore.model.container;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.MappedConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

class AutoStoreSettingSerializer {

    public static @NotNull Configuration serialize(@NotNull AutoStoreSetting setting) {
        var config = MappedConfiguration.create();

        config.set("enable", setting.isEnabled());
        config.set("all-mode", setting.isAllMode());
        config.set(
                "enabled-items",
                setting.getPerItemModeSetting()
                        .getEnabledItems()
                        .stream()
                        .map(BoxItem::getInternalId)
                        .sorted()
                        .toList()
        );

        return config;
    }

    public static @NotNull AutoStoreSetting deserializeConfiguration(@NotNull UUID uuid, @NotNull Configuration config) {
        var setting = new AutoStoreSetting(uuid);

        setting.setEnabled(config.getBoolean("enable"));
        setting.setAllMode(config.getBoolean("all-mode"));

        setting.getPerItemModeSetting().setEnabledItems(
                config.getIntegerList("enabled-items")
                        .stream()
                        .map(BoxProvider.get().getItemManager()::getBoxItem)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );

        return setting;
    }
}
