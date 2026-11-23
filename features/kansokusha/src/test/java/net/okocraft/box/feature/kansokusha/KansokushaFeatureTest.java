package net.okocraft.box.feature.kansokusha;

import dev.siroshun.event4j.api.priority.Priority;
import dev.siroshun.event4j.tree.TreeEventService;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.kansokusha.api.Kansokusha;
import net.okocraft.kansokusha.api.KansokushaApi;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class KansokushaFeatureTest {

    @Test
    void testFeatureOnlySubmitsWhileSubscribed() {
        var plugin = mock(Plugin.class);
        var server = mock(Server.class);
        var pluginManager = mock(PluginManager.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.isPluginEnabled("Kansokusha")).thenReturn(true);

        var events = TreeEventService.factory()
            .keyClass(Key.class).eventClass(BoxEvent.class).defaultOrder(Priority.NORMAL).create();
        var box = mock(BoxAPI.class);
        when(box.getListenerSubscriber()).thenReturn(events.subscriber());
        when(box.getItemManager()).thenReturn(mock(ItemManager.class));
        var api = mock(KansokushaApi.class);
        var holder = mock(StockHolder.class);
        when(holder.getUUID()).thenReturn(new UUID(0, 1));
        when(holder.getName()).thenReturn("test stock");
        var item = mock(BoxItem.class);
        var event = new StockOverflowEvent(holder, item, 1, 2, StockEvent.Cause.create("api"));
        var feature = new KansokushaFeature(mock(FeatureContext.Registration.class));

        try (var boxApi = mockStatic(BoxAPI.class); var kansokusha = mockStatic(Kansokusha.class)) {
            boxApi.when(BoxAPI::api).thenReturn(box);
            kansokusha.when(Kansokusha::api).thenReturn(api);
            feature.enable(new FeatureContext.Enabling(plugin));
            try {
                events.caller().call(event);
                verify(api).submit(any());
            } finally {
                feature.disable(new FeatureContext.Disabling(plugin));
            }
            events.caller().call(event);
            verify(api, times(1)).submit(any());
        }
    }

    @Test
    void testMissingKansokushaDoesNotAccessEitherApi() {
        var plugin = mock(Plugin.class);
        var server = mock(Server.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        var feature = new KansokushaFeature(mock(FeatureContext.Registration.class));

        try (var boxApi = mockStatic(BoxAPI.class); var kansokusha = mockStatic(Kansokusha.class)) {
            feature.enable(new FeatureContext.Enabling(plugin));
            feature.disable(new FeatureContext.Disabling(plugin));
            boxApi.verifyNoInteractions();
            kansokusha.verifyNoInteractions();
        }
    }
}
