package net.okocraft.box.feature.command.boxadmin;

import dev.siroshun.jfun.result.Result;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.customdata.DataExportPrepareEvent;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.storage.api.exporter.BoxDataFile;
import net.okocraft.box.storage.api.holder.StorageHolder;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

class ExportCommandTest {

    @Test
    void flushesStocksAndFeatureDataBeforeEncoding() throws Exception {
        BoxAPI api = Mockito.mock(BoxAPI.class);
        StockManager stocks = Mockito.mock(StockManager.class);
        List<String> order = new ArrayList<>();
        Mockito.when(api.getStockManager()).thenReturn(stocks);
        Mockito.when(api.getPluginDirectory()).thenReturn(Path.of("exports"));
        Mockito.doAnswer(_ -> { order.add("stocks"); return null; }).when(stocks).saveAll();
        // A synchronous listener registers the feature's pending-data save.
        var callers = Mockito.mock(net.okocraft.box.api.event.caller.EventCallerProvider.class);
        var caller = Mockito.mock(dev.siroshun.event4j.api.caller.EventCaller.class);
        Mockito.when(callers.sync()).thenReturn(caller);
        Mockito.when(api.getEventCallers()).thenReturn(callers);
        Mockito.doAnswer(invocation -> {
            ((DataExportPrepareEvent) invocation.getArgument(0)).addPreparation(() -> order.add("feature"));
            return null;
        }).when(caller).call(any());

        try (var boxApi = Mockito.mockStatic(BoxAPI.class);
             var holder = Mockito.mockStatic(StorageHolder.class);
             var files = Mockito.mockStatic(BoxDataFile.class)) {
            boxApi.when(BoxAPI::api).thenReturn(api);
            files.when(() -> BoxDataFile.encode(any(), isNull(), isNull(), any())).thenAnswer(_ -> {
                assertEquals(List.of("stocks", "feature"), order);
                order.add("encode");
                return Mockito.mock(Result.class, Mockito.RETURNS_SELF);
            });
            new ExportCommand((key, message) -> key).onCommand(Mockito.mock(CommandSender.class), new String[]{"export"});
            assertEquals(List.of("stocks", "feature", "encode"), order);
        }
    }
}
