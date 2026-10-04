package net.okocraft.box.feature.command.shared;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class SharedStockListCommandTest {

    @Test
    void wildcardFiltersWorkThroughCommandOutput() {
        List<BoxItem> items = List.of(item(1, "STONE"), item(2, "REDSTONE"), item(3, "DIRT"));
        String[] filters = {"*", "**", "STONE*", "*STONE", "*STONE*", "dirt"};
        int[] expected = {3, 3, 1, 2, 2, 1};
        for (int i = 0; i < filters.length; i++) {
            Component output = execute(items, new String[]{"-f", filters[i]});
            assertEquals(expected[i], countLines(output), filters[i]);
        }
    }

    @Test
    void pagePastLastPageIsClampedWithoutAnEmptyPage() {
        for (int size : new int[]{1, 8, 9, 16}) {
            List<BoxItem> items = IntStream.rangeClosed(1, size).mapToObj(id -> item(id, "item_" + id)).toList();
            Component output = execute(items, new String[]{"-p", "99"});
            assertEquals(size % 8 == 0 ? 8 : size % 8, countLines(output), "stock size " + size);
        }
    }

    private static BoxItem item(int id, String name) {
        BoxItem item = Mockito.mock(BoxItem.class);
        Mockito.when(item.getInternalId()).thenReturn(id);
        Mockito.when(item.getPlainName()).thenReturn(name);
        Mockito.when(item.getDisplayName()).thenReturn(Component.text(name));
        return item;
    }

    private static Component execute(List<BoxItem> items, String[] args) {
        StockHolder holder = Mockito.mock(StockHolder.class);
        Mockito.when(holder.getName()).thenReturn("test");
        Mockito.when(holder.getStockedItems()).thenReturn(items);
        for (BoxItem item : items) Mockito.when(holder.getAmount(item)).thenReturn(64);
        CommandSender sender = Mockito.mock(CommandSender.class);
        new SharedStockListCommand((key, message) -> key).createAndSendStockList(sender, holder, args);
        ArgumentCaptor<ComponentLike> output = ArgumentCaptor.forClass(ComponentLike.class);
        Mockito.verify(sender).sendMessage(output.capture());
        return output.getValue().asComponent();
    }

    private static long countLines(Component component) {
        long own = component instanceof TranslatableComponent translated &&
            translated.key().equals("box.command.shared.stock-list.line-format") ? 1 : 0;
        return own + component.children().stream().mapToLong(SharedStockListCommandTest::countLines).sum();
    }
}
