package net.okocraft.box.feature.autostore.model;

import com.github.siroshun09.configapi.core.node.IntValue;
import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.node.NumberValue;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

class AutoStoreSettingSerializer {

    public static @NotNull MapNode serialize(@NotNull AutoStoreSetting setting) {
        var data = MapNode.create();

        data.set("enable", setting.isEnabled());
        data.set("all-mode", setting.isAllMode());
        data.set("direct", setting.isDirect());

        var enabledItems = data.getOrCreateList("enabled-items");

        setting.getPerItemModeSetting()
                .getEnabledItems()
                .stream()
                .map(item -> new IntValue(item.getInternalId()))
                .sorted(Comparator.comparingInt(IntValue::asInt))
                .forEach(enabledItems::add);

        return data;
    }

    public static @NotNull AutoStoreSetting deserialize(@NotNull UUID uuid, @NotNull MapNode data) {
        var setting = new AutoStoreSetting(uuid);

        setting.setEnabled(data.getBoolean("enable"));
        setting.setAllMode(data.getBoolean("all-mode"));
        setting.setDirect(data.getBoolean("direct"));

        setting.getPerItemModeSetting().setEnabledItems(
                data.getList("enabled-items")
                        .asList(NumberValue.class)
                        .stream()
                        .mapToInt(NumberValue::asInt)
                        .mapToObj(BoxProvider.get().getItemManager()::getBoxItem)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );

        return setting;
    }
}
