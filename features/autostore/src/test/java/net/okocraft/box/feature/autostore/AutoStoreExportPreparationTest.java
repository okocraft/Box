package net.okocraft.box.feature.autostore;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.event4j.api.priority.Priority;
import dev.siroshun.event4j.tree.TreeEventService;
import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.customdata.DataExportPrepareEvent;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.feature.autostore.listener.CustomDataExportListener;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SuppressWarnings("deprecation")
class AutoStoreExportPreparationTest {
    @Test
    void exportPreparationPersistsLoadedSettingsWithoutADirtyEvent() throws Exception {
        BoxAPI api = Mockito.mock(BoxAPI.class);
        CustomDataManager data = Mockito.mock(CustomDataManager.class);
        Map<Key, MapNode> saved = new HashMap<>();
        Mockito.when(api.getCustomDataManager()).thenReturn(data);
        Mockito.when(data.loadData(any())).thenAnswer(_ -> MapNode.create());
        Mockito.doAnswer(invocation -> {
            saved.put(invocation.getArgument(0), ((MapNode) invocation.getArgument(1)).copy());
            return null;
        }).when(data).saveData(any(), any());
        TreeEventService<Key, BoxEvent, Priority> events = TreeEventService.factory()
            .keyClass(Key.class).eventClass(BoxEvent.class).defaultOrder(Priority.NORMAL).create();
        Mockito.when(api.getListenerSubscriber()).thenReturn(events.subscriber());
        UnsafeValues unsafe = Mockito.mock(UnsafeValues.class);
        Mockito.when(unsafe.getDataVersion()).thenReturn(5023);
        try (var boxApi = Mockito.mockStatic(BoxAPI.class); var bukkit = Mockito.mockStatic(Bukkit.class)) {
            boxApi.when(BoxAPI::api).thenReturn(api);
            bukkit.when(Bukkit::getUnsafe).thenReturn(unsafe);
            UUID uuid = UUID.randomUUID();
            Player player = Mockito.mock(Player.class);
            Mockito.when(player.getUniqueId()).thenReturn(uuid);
            AutoStoreSettingContainer container = new AutoStoreSettingContainer();
            container.load(player, MessageKey.key("load-error"));
            AutoStoreSetting setting = container.getIfLoaded(uuid);
            assertNotNull(setting);
            setting.setEnabled(true);
            setting.setAllMode(true);
            assertTrue(saved.isEmpty());
            CustomDataExportListener listener = new CustomDataExportListener();
            listener.register(Key.key("box", "test/export"), AutoStoreSettingContainer::onExportAutoStoreSetting, container::saveAll);
            try {
                DataExportPrepareEvent event = new DataExportPrepareEvent();
                events.caller().call(event);
                event.prepare();
                assertTrue(saved.get(Key.key("autostore", uuid.toString())).getBoolean("enable"));
                setting.setEnabled(false);
                DataExportPrepareEvent second = new DataExportPrepareEvent();
                events.caller().call(second);
                second.prepare();
                assertFalse(saved.get(Key.key("autostore", uuid.toString())).getBoolean("enable"));
                assertTrue(container.isLoaded(uuid));
            } finally {
                listener.unregister();
            }
        }
    }
}
