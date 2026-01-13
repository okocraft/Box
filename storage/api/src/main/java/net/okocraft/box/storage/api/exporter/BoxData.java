package net.okocraft.box.storage.api.exporter;

import dev.siroshun.codec4j.api.codec.Base64Codec;
import dev.siroshun.codec4j.api.codec.Codec;
import dev.siroshun.codec4j.api.codec.UUIDCodec;
import dev.siroshun.codec4j.api.codec.collection.ListCodec;
import dev.siroshun.codec4j.api.codec.collection.MapCodec;
import dev.siroshun.codec4j.api.codec.object.FieldCodec;
import dev.siroshun.codec4j.api.codec.object.ObjectCodec;
import dev.siroshun.codec4j.api.decoder.Decoder;
import dev.siroshun.codec4j.api.decoder.object.FieldDecoder;
import dev.siroshun.codec4j.api.decoder.object.ObjectDecoder;
import dev.siroshun.codec4j.api.error.DecodeError;
import dev.siroshun.configapi.codec.NodeCodec;
import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.jfun.result.Result;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.item.DefaultItemData;
import net.okocraft.box.storage.api.model.item.ItemData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public record BoxData(
    @NotNull MCDataVersion dataVersion,
    @NotNull Collection<BoxUser> users,
    @NotNull List<DefaultItemData> defaultItems,
    @NotNull List<ItemData> customItems,
    @NotNull Map<UUID, Collection<StockData>> stockHolders,
    @NotNull Map<Key, MapNode> customData
) {

    static final Codec<Collection<BoxUser>> USERS_CODEC = ListCodec.create(ObjectCodec.create(
        BoxUserFactory::create,
        FieldCodec.builder("uuid", UUIDCodec.UUID_AS_STRING).build(BoxUser::getUUID),
        FieldCodec.builder("name", Codec.STRING).defaultValue("").build(user -> user.getName().orElseThrow(), user -> user.getName().orElse("").isEmpty())
    )).xmap(List::copyOf, Function.identity());

    static final Codec<List<DefaultItemData>> DEFAULT_ITEMS_CODEC = ListCodec.create(ObjectCodec.create(
        DefaultItemData::new,
        FieldCodec.builder("id", Codec.INT).build(DefaultItemData::itemId),
        FieldCodec.builder("plain_name", Codec.STRING).build(DefaultItemData::plainName)
    ));

    static final Codec<List<ItemData>> CUSTOM_ITEMS_CODEC = ListCodec.create(ObjectCodec.create(
        ItemData::new,
        FieldCodec.builder("id", Codec.INT).build(ItemData::internalId),
        FieldCodec.builder("plain_name", Codec.STRING).build(ItemData::plainName),
        FieldCodec.builder("item_data", Base64Codec.CODEC).build(ItemData::itemData)
    ));

    static final Codec<StockData> STOCK_DATA_CODEC = ListCodec.create(Codec.INT).flatXmap(
        data -> Result.success(List.of(data.itemId(), data.amount())),
        list -> {
            if (list.size() != 2) {
                return Result.failure();
            }
            return Result.success(new StockData(list.get(0), list.get(1)));
        }
    );

    static final Codec<Map<UUID, Collection<StockData>>> STOCK_HOLDER_CODEC = MapCodec.create(UUIDCodec.UUID_AS_STRING, ListCodec.create(STOCK_DATA_CODEC).xmap(List::copyOf, Function.identity()));

    private static final Codec<Key> KEY_CODEC = Codec.STRING.flatXmap(
        key -> Result.success(key.asString()),
        str -> {
            try {
                return Result.success(Key.key(str));
            } catch (InvalidKeyException e) {
                return DecodeError.invalidChar(str).asFailure();
            }
        }
    );

    static final Codec<Map<Key, MapNode>> CUSTOM_DATA_CODEC = MapCodec.create(KEY_CODEC, NodeCodec.MAP_NODE_CODEC);

    public static final Decoder<BoxData> BOX_DATA_CODEC = ObjectDecoder.create(
        BoxData::new,
        FieldDecoder.required("data-version", Codec.INT.xmap(MCDataVersion::dataVersion, MCDataVersion::new)),
        FieldDecoder.required("users", USERS_CODEC),
        FieldDecoder.required("default_items", DEFAULT_ITEMS_CODEC),
        FieldDecoder.required("custom_items", CUSTOM_ITEMS_CODEC),
        FieldDecoder.required("stock", STOCK_HOLDER_CODEC),
        FieldDecoder.required("custom_data", CUSTOM_DATA_CODEC)
    );
}
