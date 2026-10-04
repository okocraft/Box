package net.okocraft.box.feature.autostore;

import dev.siroshun.event4j.api.caller.EventCaller;
import dev.siroshun.event4j.api.priority.Priority;
import dev.siroshun.event4j.tree.TreeEventService;
import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.caller.EventCallerProvider;
import net.okocraft.box.api.event.stockholder.StockHolderSaveEvent;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.feature.autostore.command.AutoStoreCommand;
import net.okocraft.box.feature.autostore.gui.AutoStoreClickMode;
import net.okocraft.box.feature.autostore.listener.AutoSaveListener;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.model.user.TestUser;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AutoStoreSaveNotificationTest {

    @Test
    void toggleAndBooleanCommandsAreSavedOnTheNextStockSave() throws Exception {
        for (String[] args : new String[][]{{"autostore"}, {"autostore", "on"}, {"autostore", "off"}}) {
            Fixture fixture = new Fixture();
            fixture.setting.setEnabled(args.length == 2 && args[1].equals("off"));
            try (var boxApi = Mockito.mockStatic(BoxAPI.class)) {
                boxApi.when(BoxAPI::api).thenReturn(fixture.api);
                fixture.listener.register(Key.key("box", "test/autostore"));
                try {
                    new AutoStoreCommand(fixture.container, MessageKey.key("load-error"), (key, message) -> key)
                        .onCommand(fixture.player, args);
                    fixture.saveStock();
                    Mockito.verify(fixture.container).save(fixture.setting);
                } finally {
                    fixture.listener.unregister();
                }
            }
        }
    }

    @Test
    void unchangedBooleanCommandDoesNotMarkSettingsDirty() throws Exception {
        Fixture fixture = new Fixture();
        fixture.setting.setEnabled(true);
        try (var boxApi = Mockito.mockStatic(BoxAPI.class)) {
            boxApi.when(BoxAPI::api).thenReturn(fixture.api);
            fixture.listener.register(Key.key("box", "test/autostore"));
            try {
                new AutoStoreCommand(fixture.container, MessageKey.key("load-error"), (key, message) -> key)
                    .onCommand(fixture.player, new String[]{"autostore", "on"});
                fixture.saveStock();
                Mockito.verify(fixture.container, Mockito.never()).save(any());
            } finally {
                fixture.listener.unregister();
            }
        }
    }

    @Test
    void guiItemToggleIsSavedWithoutLoggingOut() throws Exception {
        Fixture fixture = new Fixture();
        StockManager stocks = Mockito.mock(StockManager.class);
        Mockito.when(fixture.api.getStockManager()).thenReturn(stocks);
        Mockito.when(stocks.getPersonalStockHolder(TestUser.USER)).thenReturn(fixture.holder);
        try (var boxApi = Mockito.mockStatic(BoxAPI.class)) {
            boxApi.when(BoxAPI::api).thenReturn(fixture.api);
            fixture.listener.register(Key.key("box", "test/autostore"));
            try {
                PlayerSession session = PlayerSession.newSession(fixture.player, TestUser.USER);
                session.putData(AutoStoreSetting.KEY, fixture.setting);
                DummyItem stone = new DummyItem(1, "STONE");
                new AutoStoreClickMode(fixture.container, (key, message) -> key).onClick(session, stone, ClickType.LEFT);
                fixture.saveStock();
                Mockito.verify(fixture.container).save(fixture.setting);
                assertTrue(fixture.setting.isEnabled());
                assertTrue(fixture.setting.shouldAutoStore(stone));
                assertFalse(fixture.setting.shouldAutoStore(new DummyItem(2, "DIRT")));
            } finally {
                fixture.listener.unregister();
            }
        }
    }

    private static final class Fixture {
        private final BoxAPI api = Mockito.mock(BoxAPI.class);
        private final AutoStoreSettingProvider container = Mockito.mock(AutoStoreSettingProvider.class);
        private final AutoStoreSetting setting = new AutoStoreSetting(TestUser.USER.getUUID());
        private final Player player = Mockito.mock(Player.class);
        private final PersonalStockHolder holder = Mockito.mock(PersonalStockHolder.class);
        private final AutoSaveListener listener = new AutoSaveListener(this.container);
        private final TreeEventService<Key, BoxEvent, Priority> events = TreeEventService.factory()
            .keyClass(Key.class).eventClass(BoxEvent.class).defaultOrder(Priority.NORMAL).create();

        private Fixture() {
            Mockito.when(this.player.getUniqueId()).thenReturn(TestUser.USER.getUUID());
            Mockito.when(this.holder.getUser()).thenReturn(TestUser.USER);
            Mockito.when(this.container.getIfLoaded(TestUser.USER.getUUID())).thenReturn(this.setting);
            Mockito.when(this.api.getListenerSubscriber()).thenReturn(this.events.subscriber());
            Mockito.when(this.api.getEventCallers()).thenReturn(new EventCallerProvider() {
                @Override public EventCaller<BoxEvent> sync() { return events.caller(); }
                @Override public EventCaller<BoxEvent> async() { return events.caller(); }
            });
        }

        private void saveStock() {
            this.events.caller().call(new StockHolderSaveEvent(this.holder));
        }
    }
}
