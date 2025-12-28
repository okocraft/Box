package net.okocraft.box.feature.autostore;

import dev.siroshun.configapi.core.node.IntArray;
import dev.siroshun.configapi.core.node.ListNode;
import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.core.node.NumberValue;
import dev.siroshun.event4j.api.priority.Priority;
import dev.siroshun.mcmsgdef.MessageKey;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.customdata.CustomDataExportEvent;
import net.okocraft.box.api.event.player.PlayerLoadEvent;
import net.okocraft.box.api.event.player.PlayerUnloadEvent;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class AutoStoreSettingContainer implements AutoStoreSettingProvider {

    private static final @NotNull Key PLAYER_LISTENER_KEY = Key.key("box", "feature/autostore/player_listener");

    private final Map<UUID, AutoStoreSetting> settingMap = new ConcurrentHashMap<>();
    private final SubscribedListenerHolder listenerHolder = new SubscribedListenerHolder();

    @Override
    public boolean isLoaded(@NotNull UUID uuid) {
        return this.settingMap.containsKey(uuid);
    }

    @Override
    public @Nullable AutoStoreSetting getIfLoaded(@NotNull UUID uuid) {
        return this.settingMap.get(uuid);
    }

    @Override
    public @NotNull AutoStoreSetting getOrLoad(@NotNull UUID uuid) throws Exception {
        var loaded = this.settingMap.get(uuid);
        return loaded != null ? loaded : this.load(uuid);
    }

    @Override
    public void save(@NotNull AutoStoreSetting setting) throws Exception {
        BoxAPI.api().getCustomDataManager().saveData(createKey(setting.getUuid()), serialize(setting));
    }

    void registerBoxPlayerListener(@NotNull MessageKey loadErrorMessage) {
        this.listenerHolder.subscribeAll(subscriber ->
            subscriber.add(PlayerLoadEvent.class, PLAYER_LISTENER_KEY, event -> this.load(event.getBoxPlayer().getPlayer(), loadErrorMessage), Priority.NORMAL)
                .add(PlayerUnloadEvent.class, PLAYER_LISTENER_KEY, event -> this.unload(event.getBoxPlayer().getPlayer()), Priority.NORMAL)
        );
    }

    void unregisterBoxPlayerListener() {
        this.listenerHolder.unsubscribeAll();
    }

    void load(@NotNull Player player, @NotNull MessageKey loadErrorMessage) {
        try {
            this.settingMap.put(player.getUniqueId(), this.load(player.getUniqueId()));
        } catch (Exception e) {
            BoxLogger.logger().error("Could not load autostore setting ({})", player.getName(), e);
            player.sendMessage(loadErrorMessage);
        }
    }

    void unload(@NotNull Player player) {
        try {
            var setting = this.settingMap.remove(player.getUniqueId());

            if (setting != null) {
                this.save(setting);
            }
        } catch (Exception e) {
            BoxLogger.logger().error("Could not unload autostore setting ({})", player.getName(), e);
        }
    }

    void unloadAll() {
        for (var setting : this.settingMap.values()) {
            try {
                this.save(setting);
            } catch (Exception e) {
                BoxLogger.logger().error("Could not unload autostore setting ({})", setting.getUuid(), e);
            }
        }
        this.settingMap.clear();
    }

    private @NotNull AutoStoreSetting load(@NotNull UUID uuid) throws Exception {
        return deserialize(uuid, BoxAPI.api().getCustomDataManager().loadData(createKey(uuid)));
    }

    @SuppressWarnings("PatternValidation")
    private static @NotNull Key createKey(@NotNull UUID uuid) {
        return Key.key("autostore", uuid.toString());
    }

    private static @NotNull MapNode serialize(@NotNull AutoStoreSetting setting) {
        var data = MapNode.create();

        if (setting.isEnabled()) data.set("enable", true);
        if (setting.isAllMode()) data.set("all-mode", true);
        if (setting.isDirect()) data.set("direct", true);

        var items = setting.getPerItemModeSetting().getEnabledItems();
        if (!items.isEmpty()) data.set("enabled-items", items.toIntArray());

        if (!data.isEmpty()) {
            data.set("data-version", MCDataVersion.current().dataVersion());
        }

        return data;
    }

    private static @NotNull AutoStoreSetting deserialize(@NotNull UUID uuid, @NotNull MapNode data) {
        var setting = new AutoStoreSetting(uuid);

        setting.setEnabled(data.getBoolean("enable"));
        setting.setAllMode(data.getBoolean("all-mode"));
        setting.setDirect(data.getBoolean("direct"));

        var dataVersion = data.getInteger("data-version", MCDataVersion.MC_1_20_4.dataVersion());

        var enabledItemsNode = data.get("enabled-items");
        IntList enabledItemIds;

        if (enabledItemsNode instanceof IntArray(int[] value)) {
            enabledItemIds = IntArrayList.of(value);
        } else if (enabledItemsNode instanceof ListNode list) {
            enabledItemIds = IntArrayList.toList(
                list.asList(NumberValue.class).stream().mapToInt(NumberValue::asInt)
            );
        } else {
            enabledItemIds = IntList.of();
        }

        if (!enabledItemIds.isEmpty() && dataVersion != MCDataVersion.current().dataVersion()) {
            var idMap = BoxAPI.api().getItemManager().getRemappedItemIds();
            enabledItemIds = IntArrayList.toList(enabledItemIds.intStream().map(id -> idMap.getOrDefault(id, id)));
        }

        setting.getPerItemModeSetting().clearAndEnableItems(enabledItemIds);

        return setting;
    }

    static void onExportAutoStoreSetting(CustomDataExportEvent event) {
        if (!event.getKey().namespace().equals("autostore")) {
            return;
        }

        event.editNode(AutoStoreSettingContainer::trimMapNode);
    }

    private static void trimMapNode(MapNode target) {
        MapNode source = target.copy();
        target.clear();

        if (source.getBoolean("enable")) {
            target.set("enable", true);
        }

        if (source.getBoolean("all-mode")) {
            target.set("all-mode", true);
        }

        if (source.getBoolean("direct")) {
            target.set("direct", true);
        }

        var enabledItemsNode = source.get("enabled-items");
        if (enabledItemsNode instanceof IntArray(int[] value) && 0 < value.length) {
            target.set("enabled-items", value);
        } else if (enabledItemsNode instanceof ListNode list) {
            int[] ids = list.asList(NumberValue.class).stream().mapToInt(NumberValue::asInt).toArray();
            if (0 < ids.length) {
                target.set("enabled-items", new IntArray(ids));
            }
        }

        if (!target.isEmpty()) {
            int dataVersion = source.getInteger("data-version");
            if (dataVersion != 0) {
                target.set("data-version", dataVersion);
            }
        }
    }
}
