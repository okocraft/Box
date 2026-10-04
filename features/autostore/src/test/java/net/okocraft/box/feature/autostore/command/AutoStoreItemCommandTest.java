package net.okocraft.box.feature.autostore.command;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import net.okocraft.box.test.shared.event.EventCollector;
import net.okocraft.box.test.shared.model.item.DummyItem;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AutoStoreItemCommandTest {

    private static final DummyItem STONE = new DummyItem(1, "STONE");
    private static final DummyItem DIRT = new DummyItem(2, "DIRT");

    @Test
    void itemCommandsSwitchFromAllModeAndApplySelections() {
        BoxAPI api = Mockito.mock(BoxAPI.class);
        ItemManager items = Mockito.mock(ItemManager.class);
        EventCollector events = new EventCollector();
        Mockito.when(api.getItemManager()).thenReturn(items);
        Mockito.when(api.getEventCallers()).thenReturn(events);
        Mockito.when(items.getBoxItem("STONE")).thenReturn(Optional.of(STONE));
        Mockito.when(items.getBoxItem("all")).thenReturn(Optional.empty());
        Mockito.when(items.getItemIdList()).thenReturn(IntImmutableList.of(1, 2));

        try (var boxApi = Mockito.mockStatic(BoxAPI.class)) {
            boxApi.when(BoxAPI::api).thenReturn(api);
            AutoStoreItemCommand command = new AutoStoreItemCommand((key, message) -> key);
            AutoStoreCommandUtil.addToggleMessages((key, message) -> key);
            String[][] arguments = {
                {"autostore", "item", "STONE", "off"},
                {"autostore", "item", "STONE", "on"},
                {"autostore", "item", "STONE"},
                {"autostore", "item", "all", "off"},
                {"autostore", "item", "all", "on"}
            };
            for (int i = 0; i < arguments.length; i++) {
                AutoStoreSetting setting = new AutoStoreSetting(UUID.randomUUID());
                setting.setEnabled(true);
                command.runCommand(Mockito.mock(CommandSender.class), arguments[i], setting);
                assertFalse(setting.isAllMode());
                assertEquals(i == 1 || i == 2 || i == 4, setting.shouldAutoStore(STONE));
                assertEquals(i == 4, setting.shouldAutoStore(DIRT));
                events.checkEvent(AutoStoreSettingChangeEvent.class, event -> assertSame(setting, event.getSetting()));
                events.checkNoEvent();
            }
        }
    }
}
