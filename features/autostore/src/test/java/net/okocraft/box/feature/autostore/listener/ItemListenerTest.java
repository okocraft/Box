package net.okocraft.box.feature.autostore.listener;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.caller.EventCallerProvider;
import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.feature.autostore.AutoStoreSettingProvider;
import net.okocraft.box.feature.autostore.event.AutoStoreItemEvent;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import net.okocraft.box.test.shared.model.item.DummyItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemListenerTest {

    @Test
    void testOnlyStoredDropsArePublishedAfterRemovalWithoutAuditPlugins() {
        var playerId = new UUID(0, 1);
        var entityId = new UUID(0, 2);
        var location = new Location(null, 4, 5, 6);
        var player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("Collector");
        when(player.hasPermission("box.autostore")).thenReturn(true);
        var block = mock(Block.class);
        when(block.getLocation()).thenReturn(location);
        var item = mock(ItemStack.class);
        var snapshot = mock(ItemStack.class);
        when(item.clone()).thenReturn(snapshot);
        when(item.getAmount()).thenReturn(3);
        var accepted = mock(Item.class);
        when(accepted.getItemStack()).thenReturn(item);
        when(accepted.getUniqueId()).thenReturn(entityId);
        var rejected = mock(Item.class);
        when(rejected.getItemStack()).thenReturn(mock(ItemStack.class));
        var drops = new ArrayList<>(List.of(accepted, rejected));
        var event = mock(BlockDropItemEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getBlock()).thenReturn(block);
        when(event.getItems()).thenReturn(drops);

        var api = mock(BoxAPI.class);
        var players = mock(BoxPlayerMap.class);
        var boxPlayer = mock(BoxPlayer.class);
        var holder = mock(StockHolder.class);
        var items = mock(ItemManager.class);
        var setting = new AutoStoreSetting(playerId);
        setting.setEnabled(true);
        setting.setDirect(true);
        var settings = mock(AutoStoreSettingProvider.class);
        when(settings.getIfLoaded(playerId)).thenReturn(setting);
        when(api.canUseBox(player)).thenReturn(true);
        when(api.getBoxPlayerMap()).thenReturn(players);
        when(players.isLoaded(player)).thenReturn(true);
        when(players.get(player)).thenReturn(boxPlayer);
        when(boxPlayer.getCurrentStockHolder()).thenReturn(holder);
        when(api.getItemManager()).thenReturn(items);
        var boxItem = new DummyItem(1, "STONE");
        when(items.getBoxItem(item)).thenReturn(Optional.of(boxItem));

        var callers = mock(EventCallerProvider.class);
        var emitted = new ArrayList<BoxEvent>();
        when(api.getEventCallers()).thenReturn(callers);
        when(callers.sync()).thenReturn(notification -> {
            assertEquals(List.of(rejected), drops);
            emitted.add(notification);
        });

        try (var box = mockStatic(BoxAPI.class); var bukkit = mockStatic(Bukkit.class)) {
            box.when(BoxAPI::api).thenReturn(api);
            var plugins = mock(PluginManager.class);
            bukkit.when(Bukkit::getPluginManager).thenReturn(plugins);
            new ItemListener(settings).onBlockDropItem(event);
        }

        verify(holder).increase(eq(boxItem), eq(3), any());
        var notification = assertInstanceOf(AutoStoreItemEvent.class, emitted.getFirst());
        assertEquals(1, emitted.size());
        assertEquals(new StockActor.Player(playerId, "Collector"), notification.getActor());
        assertEquals(entityId, notification.getItemEntityId());
        assertEquals(location, notification.getLocation());
        assertNotSame(location, notification.getLocation());
    }
}
