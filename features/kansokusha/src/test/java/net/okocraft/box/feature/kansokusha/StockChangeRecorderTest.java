package net.okocraft.box.feature.kansokusha;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockOverflowEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.kansokusha.api.KansokushaApi;
import net.okocraft.kansokusha.api.actor.PlayerActor;
import net.okocraft.kansokusha.api.event.EventSubmission;
import net.okocraft.kansokusha.api.event.EventTypeDefinition;
import net.okocraft.kansokusha.api.event.PayloadGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockChangeRecorderTest {

    private static final Key EVENT_TYPE = Key.key("box", "stock_change");
    private static final Key SERVER = Key.key("test", "paper");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final KansokushaApi api = mock(KansokushaApi.class);
    private final ItemManager itemManager = mock(ItemManager.class);
    private final StockHolder holder = mock(StockHolder.class);
    private final BoxItem stone = mock(BoxItem.class);

    @Test
    void testDecreaseRecordsInitiatingPlayerAndStockSeparately() {
        var recorder = this.createRecorder();
        var cause = StockEvent.Cause.create("withdraw", new StockActor.Player(PLAYER_ID));
        var event = new StockDecreaseEvent(this.holder, this.stone, 4, 7, cause);

        recorder.onStockChange(event);

        verify(this.api).registerEventType(new EventTypeDefinition(EVENT_TYPE, PayloadGeneration.FIRST));
        var submission = this.onlySubmission();
        assertEquals(EVENT_TYPE, submission.eventType());
        assertEquals(PayloadGeneration.FIRST, submission.payloadGeneration());
        assertEquals(event.getOccurredAt(), submission.occurredAt());
        assertEquals(SERVER, submission.serverKey());
        assertEquals(new PlayerActor(PLAYER_ID), submission.actor());
        assertNull(submission.worldKey());
        assertNull(submission.position());
        var payload = payload(submission);
        assertEquals(OWNER_ID.toString(), payload.get("stock_holder_uuid").getAsString());
        assertEquals("共有在庫", payload.get("stock_holder_name").getAsString());
        assertEquals(12, payload.get("item_id").getAsInt());
        assertEquals("STONE", payload.get("item_name").getAsString());
        assertEquals("withdraw", payload.get("cause").getAsString());
        assertAmounts(payload, 11, 7, -4);
        assertFalse(payload.has("actor_kind"));
    }

    @Test
    void testPartialOverflowRecordsOnlyAcceptedAmount() {
        var recorder = this.createRecorder();
        recorder.onStockChange(new StockOverflowEvent(this.holder, this.stone, 2, 9, StockEvent.Cause.create("api")));
        var submission = this.onlySubmission();
        assertNull(submission.actor());
        assertAmounts(payload(submission), Integer.MAX_VALUE - 2, Integer.MAX_VALUE, 2);
    }

    @Test
    void testNoChangeDoesNotSubmit() {
        var recorder = this.createRecorder();
        var cause = StockEvent.Cause.create("api");
        recorder.onStockChange(new StockOverflowEvent(this.holder, this.stone, 0, 4, cause));
        recorder.onStockChange(new StockSetEvent(this.holder, this.stone, 5, 5, cause));
        recorder.onReset(new StockHolderResetEvent(this.holder, List.of()));

        verify(this.api, never()).submit(any());
    }

    @Test
    void testIncreaseAndSetUseEventAmountsNotCurrentHolderAmount() {
        var recorder = this.createRecorder();
        var cause = StockEvent.Cause.create("api");
        recorder.onStockChange(new StockIncreaseEvent(this.holder, this.stone, 2, 5, cause));
        recorder.onStockChange(new StockSetEvent(this.holder, this.stone, 1, 5, cause));

        var submissions = ArgumentCaptor.forClass(EventSubmission.class);
        verify(this.api, times(2)).submit(submissions.capture());
        assertAmounts(payload(submissions.getAllValues().get(0)), 3, 5, 2);
        assertAmounts(payload(submissions.getAllValues().get(1)), 5, 1, -4);
        verify(this.holder, never()).getAmount(anyInt());
    }

    @Test
    void testConsoleResetSubmitsOneChangePerItemEvenWhenNameIsMissing() {
        var recorder = this.createRecorder();
        when(this.itemManager.getBoxItemOrNull(12)).thenReturn(this.stone);
        var cause = StockEvent.Cause.create("admin_reset", StockActor.Console.INSTANCE);
        var event = new StockHolderResetEvent(this.holder, List.of(new StockData(12, 7), new StockData(42, 3)), cause);

        recorder.onReset(event);

        var captor = ArgumentCaptor.forClass(EventSubmission.class);
        verify(this.api, times(2)).submit(captor.capture());
        var rows = captor.getAllValues();
        for (var row : rows) {
            assertEquals(event.getOccurredAt(), row.occurredAt());
            assertNull(row.actor());
            assertEquals("console", payload(row).get("actor_kind").getAsString());
            assertEquals("admin_reset", payload(row).get("cause").getAsString());
        }
        assertEquals("STONE", payload(rows.get(0)).get("item_name").getAsString());
        assertAmounts(payload(rows.get(0)), 7, 0, -7);
        assertEquals(42, payload(rows.get(1)).get("item_id").getAsInt());
        assertTrue(payload(rows.get(1)).get("item_name").isJsonNull());
        assertAmounts(payload(rows.get(1)), 3, 0, -3);
    }

    private StockChangeRecorder createRecorder() {
        when(this.holder.getUUID()).thenReturn(OWNER_ID);
        when(this.holder.getName()).thenReturn("共有在庫");
        when(this.stone.getInternalId()).thenReturn(12);
        when(this.stone.getPlainName()).thenReturn("STONE");
        when(this.api.localServerKey()).thenReturn(Optional.of(SERVER));
        return new StockChangeRecorder(this.api, this.itemManager);
    }

    private EventSubmission onlySubmission() {
        var captor = ArgumentCaptor.forClass(EventSubmission.class);
        verify(this.api).submit(captor.capture());
        return captor.getValue();
    }

    private static JsonObject payload(EventSubmission submission) {
        return JsonParser.parseString(new String(submission.payload().copyBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void assertAmounts(JsonObject payload, int before, int after, int delta) {
        assertEquals(before, payload.get("before").getAsInt());
        assertEquals(after, payload.get("after").getAsInt());
        assertEquals(delta, payload.get("delta").getAsInt());
    }
}
