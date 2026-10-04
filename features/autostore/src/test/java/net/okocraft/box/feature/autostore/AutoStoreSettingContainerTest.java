package net.okocraft.box.feature.autostore;

import dev.siroshun.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.customdata.CustomDataExportEvent;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import net.okocraft.box.test.shared.model.item.DummyItem;
import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SuppressWarnings("deprecation")
class AutoStoreSettingContainerTest {

    private static final DummyItem ITEM = new DummyItem(1, "STONE");
    private final Map<Key, MapNode> savedData = new HashMap<>();
    private MockedStatic<BoxAPI> boxApi;
    private MockedStatic<Bukkit> bukkit;

    @BeforeEach
    void setUp() throws Exception {
        BoxAPI api = Mockito.mock(BoxAPI.class);
        CustomDataManager dataManager = Mockito.mock(CustomDataManager.class);
        Mockito.when(api.getCustomDataManager()).thenReturn(dataManager);
        Mockito.when(dataManager.loadData(any())).thenAnswer(invocation ->
            this.savedData.getOrDefault(invocation.getArgument(0), MapNode.create()).copy());
        Mockito.doAnswer(invocation -> {
            this.savedData.put(invocation.getArgument(0), ((MapNode) invocation.getArgument(1)).copy());
            return null;
        }).when(dataManager).saveData(any(), any());
        this.boxApi = Mockito.mockStatic(BoxAPI.class);
        this.boxApi.when(BoxAPI::api).thenReturn(api);
        UnsafeValues unsafe = Mockito.mock(UnsafeValues.class);
        Mockito.when(unsafe.getDataVersion()).thenReturn(5023);
        this.bukkit = Mockito.mockStatic(Bukkit.class);
        this.bukkit.when(Bukkit::getUnsafe).thenReturn(unsafe);
    }

    @AfterEach
    void tearDown() {
        this.bukkit.close();
        this.boxApi.close();
    }

    @Test
    void newPlayerUsesAllMode() throws Exception {
        AutoStoreSetting setting = new AutoStoreSettingContainer().getOrLoad(UUID.randomUUID());
        assertFalse(setting.isEnabled());
        assertTrue(setting.isAllMode());
        setting.setEnabled(true);
        assertTrue(setting.shouldAutoStore(ITEM));
    }

    @Test
    void legacyEmptyDataUsesNewDefaultsAndStaysDisabledUntilEnabled() throws Exception {
        UUID uuid = UUID.randomUUID();
        Key key = Key.key("autostore", uuid.toString());
        // The old serializer emitted an empty node for disabled per-item mode
        // with no selected items. Its load result is identical to missing data.
        this.savedData.put(key, MapNode.create());
        AutoStoreSettingContainer container = new AutoStoreSettingContainer();
        AutoStoreSetting setting = container.getOrLoad(uuid);
        assertFalse(setting.isEnabled());
        assertTrue(setting.isAllMode());
        assertFalse(setting.shouldAutoStore(ITEM));
        setting.setEnabled(true);
        assertTrue(setting.shouldAutoStore(ITEM));
        container.save(setting);
        assertTrue(this.savedData.get(key).getBoolean("all-mode"));
    }

    @Test
    void disabledEmptyPerItemModeSurvivesSaveAndExport() throws Exception {
        UUID uuid = UUID.randomUUID();
        AutoStoreSettingContainer container = new AutoStoreSettingContainer();
        AutoStoreSetting setting = new AutoStoreSetting(uuid);
        setting.setAllMode(false);
        container.save(setting);

        AutoStoreSetting reloaded = new AutoStoreSettingContainer().getOrLoad(uuid);
        assertFalse(reloaded.isEnabled());
        assertFalse(reloaded.isAllMode());
        assertFalse(reloaded.shouldAutoStore(ITEM));

        Key key = Key.key("autostore", uuid.toString());
        CustomDataExportEvent event = new CustomDataExportEvent(key, this.savedData.get(key));
        AutoStoreSettingContainer.onExportAutoStoreSetting(event);
        assertFalse(event.getResultNode().isEmpty());
        this.savedData.put(key, event.getResultNode());
        assertFalse(new AutoStoreSettingContainer().getOrLoad(uuid).isAllMode());
    }

    @Test
    void enabledPerItemSelectionSurvivesReload() throws Exception {
        UUID uuid = UUID.randomUUID();
        AutoStoreSetting setting = new AutoStoreSetting(uuid);
        setting.setEnabled(true);
        setting.setAllMode(false);
        setting.getPerItemModeSetting().setEnabled(ITEM, true);
        new AutoStoreSettingContainer().save(setting);
        AutoStoreSetting reloaded = new AutoStoreSettingContainer().getOrLoad(uuid);
        assertTrue(reloaded.isEnabled());
        assertFalse(reloaded.isAllMode());
        assertTrue(reloaded.shouldAutoStore(ITEM));
        assertFalse(reloaded.shouldAutoStore(new DummyItem(2, "DIRT")));
    }
}
