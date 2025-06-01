package net.okocraft.box.storage.api.exporter;

import dev.siroshun.codec4j.api.encoder.Encoder;
import dev.siroshun.codec4j.api.error.EncodeError;
import dev.siroshun.codec4j.api.io.Out;
import dev.siroshun.configapi.codec.NodeCodec;
import dev.siroshun.event4j.api.caller.EventCaller;
import dev.siroshun.jfun.result.Result;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.customdata.CustomDataExportEvent;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxDefaultItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.DefaultItemData;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

final class DataExporter {

    static Encoder<Void> createEncoder(Storage storage, ItemManager itemManager, EventCaller<BoxEvent> eventCaller) {
        return new Encoder<>() {
            @Override
            public @NotNull <O> Result<O, EncodeError> encode(@NotNull Out<O> out, @UnknownNullability Void input) {
                return out.createMap().flatMap(
                    appender -> {
                        var dataVersionResult = appender.append(o -> o.writeString("data-version"), o -> o.writeInt(MCDataVersion.current().dataVersion()));
                        if (dataVersionResult.isFailure()) {
                            return dataVersionResult.asFailure();
                        }

                        var usersResult = appender.append(o -> o.writeString("users"), o -> encodeUsers(o, storage.getUserStorage()));
                        if (usersResult.isFailure()) {
                            return usersResult.asFailure();
                        }

                        var stockResult = appender.append(o -> o.writeString("stock"), o -> encodeStockHolders(o, storage.getStockStorage()));
                        if (stockResult.isFailure()) {
                            return stockResult.asFailure();
                        }

                        var defaultItemResult = appender.append(o -> o.writeString("default_items"), o -> encodeDefaultItems(o, itemManager));
                        if (defaultItemResult.isFailure()) {
                            return defaultItemResult.asFailure();
                        }

                        var customItemResult = appender.append(o -> o.writeString("custom_items"), o -> encodeCustomItems(o, itemManager));
                        if (customItemResult.isFailure()) {
                            return customItemResult.asFailure();
                        }

                        var customDataResult = appender.append(o -> o.writeString("custom_data"), o -> encodeCustomData(o, storage.getCustomDataStorage(), eventCaller));
                        if (customDataResult.isFailure()) {
                            return customDataResult.asFailure();
                        }
                        return appender.finish();
                    },
                    Result::failure
                );
            }
        };
    }

    private static <O> @NotNull Result<O, EncodeError> encodeUsers(Out<O> out, UserStorage storage) {
        Collection<BoxUser> users;
        try {
            users = storage.loadAllBoxUsers();
        } catch (Exception e) {
            return EncodeError.fatalError(e).asFailure();
        }

        return BoxData.USERS_CODEC.encode(out, users);
    }

    private static <O> Result<O, EncodeError> encodeStockHolders(Out<O> out, StockStorage storage) {
        Map<UUID, Collection<StockData>> map;
        try {
            map = storage.loadAllStockData();
        } catch (Exception e) {
            return EncodeError.fatalError(e).asFailure();
        }

        return BoxData.STOCK_HOLDER_CODEC.encode(out, map);
    }

    private static <O> Result<O, EncodeError> encodeDefaultItems(Out<O> out, ItemManager itemManager) {
        return BoxData.DEFAULT_ITEMS_CODEC.encode(
            out,
            itemManager.getItemList()
                .stream()
                .filter(BoxDefaultItem.class::isInstance)
                .map(item -> new DefaultItemData(item.getInternalId(), item.getPlainName()))
                .toList()
        );
    }

    private static <O> Result<O, EncodeError> encodeCustomItems(Out<O> out, ItemManager itemManager) {
        return BoxData.CUSTOM_ITEMS_CODEC.encode(
            out,
            itemManager.getItemList()
                .stream()
                .filter(BoxCustomItem.class::isInstance)
                .map(item -> new ItemData(item.getInternalId(), item.getPlainName(), item.getOriginal().serializeAsBytes()))
                .toList()
        );
    }

    private static <O> Result<O, EncodeError> encodeCustomData(Out<O> out, CustomDataStorage storage, EventCaller<BoxEvent> eventCaller) {
        return out.createMap().flatMap(
            appender -> {
                var ref = new AtomicReference<Result<Void, EncodeError>>();
                try {
                    storage.visitAllData((key, mapNode) -> {
                        var event = new CustomDataExportEvent(key, mapNode);
                        eventCaller.call(event);

                        var resultNode = event.getResultNode();
                        if (event.isCancelled() || resultNode.isEmpty()) {
                            return;
                        }

                        var result = appender.append(o -> o.writeString(key.asString()), o -> NodeCodec.MAP_NODE_CODEC.encode(o, resultNode));
                        if (result.isFailure()) {
                            ref.set(result);
                            throw new RuntimeException();
                        }
                    });
                } catch (Exception e) {
                    if (ref.get() != null) {
                        return ref.get().asFailure();
                    }
                    return EncodeError.fatalError(e).asFailure();
                }
                return appender.finish();
            },
            EncodeError::asFailure
        );
    }
}
