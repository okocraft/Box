package net.okocraft.box.feature.kansokusha;

import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.event.stockholder.stock.StockDecreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.event.stockholder.stock.StockIncreaseEvent;
import net.okocraft.box.api.event.stockholder.stock.StockSetEvent;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.kansokusha.api.Kansokusha;
import net.okocraft.kansokusha.api.KansokushaApi;
import net.okocraft.kansokusha.api.actor.PlayerActor;
import net.okocraft.kansokusha.api.event.EventPayload;
import net.okocraft.kansokusha.api.event.EventSubmission;
import net.okocraft.kansokusha.api.event.EventTypeDefinition;
import net.okocraft.kansokusha.api.event.PayloadGeneration;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@NotNullByDefault
final class StockChangeRecorder {

    private static final EventTypeDefinition STOCK_CHANGE =
        new EventTypeDefinition(Key.key("box", "stock_change"), PayloadGeneration.FIRST);

    private final KansokushaApi api;
    private final ItemManager itemManager;
    private final @Nullable Key serverKey;

    static StockChangeRecorder connect(ItemManager itemManager) {
        return new StockChangeRecorder(Kansokusha.api(), itemManager);
    }

    StockChangeRecorder(KansokushaApi api, ItemManager itemManager) {
        this.api = api;
        this.itemManager = itemManager;
        this.serverKey = api.localServerKey().orElse(null);
        api.registerEventType(STOCK_CHANGE);
    }

    void onStockChange(StockEvent event) {
        int after = event.getAmount();
        int before = switch (event) {
            // StockOverflowEvent is an increase whose increments already exclude the excess.
            case StockIncreaseEvent increase -> after - increase.getIncrements();
            case StockDecreaseEvent decrease -> after + decrease.getDecrements();
            case StockSetEvent set -> set.getPreviousAmount();
            default -> after;
        };

        if (before != after) {
            var item = event.getItem();
            this.record(event.getOccurredAt(), event.getStockHolder(), item.getInternalId(), item.getPlainName(),
                before, after, event.getCause().name(), event.getCause().actor());
        }
    }

    void onReset(StockHolderResetEvent event) {
        var cause = event.getCause();
        var actor = cause.actor();
        for (var stock : event.getStockDataBeforeReset()) {
            if (stock.amount() > 0) {
                var item = this.itemManager.getBoxItemOrNull(stock.itemId());
                this.record(event.getOccurredAt(), event.getStockHolder(), stock.itemId(),
                    item != null ? item.getPlainName() : null, stock.amount(), 0, cause.name(), actor);
            }
        }
    }

    private void record(Instant occurredAt, StockHolder holder, int itemId, @Nullable String itemName,
                        int before, int after, String cause, @Nullable StockActor actor) {
        var payload = new JsonObject();
        payload.addProperty("stock_holder_uuid", holder.getUUID().toString());
        payload.addProperty("stock_holder_name", holder.getName());
        payload.addProperty("item_id", itemId);
        payload.addProperty("item_name", itemName);
        payload.addProperty("before", before);
        payload.addProperty("after", after);
        payload.addProperty("delta", after - before);
        payload.addProperty("cause", cause);
        if (actor == StockActor.Console.INSTANCE) {
            payload.addProperty("actor_kind", "console");
        }

        this.api.submit(new EventSubmission(STOCK_CHANGE.key(), STOCK_CHANGE.payloadGeneration(), occurredAt,
            this.serverKey, null, null, actor instanceof StockActor.Player player ? new PlayerActor(player.uuid()) : null,
            null, EventPayload.copyOf(payload.toString().getBytes(StandardCharsets.UTF_8))));
    }
}
