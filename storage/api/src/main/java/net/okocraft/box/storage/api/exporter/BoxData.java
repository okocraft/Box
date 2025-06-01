package net.okocraft.box.storage.api.exporter;

import dev.siroshun.codec4j.api.codec.Base64Codec;
import dev.siroshun.codec4j.api.codec.Codec;
import dev.siroshun.codec4j.api.codec.UUIDCodec;
import dev.siroshun.codec4j.api.codec.object.ObjectCodec;
import dev.siroshun.codec4j.api.decoder.Decoder;
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

public record BoxData(
    @NotNull MCDataVersion dataVersion,
    @NotNull Collection<BoxUser> users,
    @NotNull List<DefaultItemData> defaultItems,
    @NotNull List<ItemData> customItems,
    @NotNull Map<UUID, Collection<StockData>> stockHolders,
    @NotNull Map<Key, MapNode> customData
) {

    static final Codec<Collection<BoxUser>> USERS_CODEC = ObjectCodec.create(
        BoxUserFactory::create,
        UUIDCodec.UUID_AS_STRING.toFieldCodec("uuid").build(BoxUser::getUUID),
        Codec.STRING.toFieldCodec("name").defaultValue("").build(user -> user.getName().orElseThrow(), user -> user.getName().orElse("").isEmpty())
    ).toCollectionCodec();

    static final Codec<List<DefaultItemData>> DEFAULT_ITEMS_CODEC = ObjectCodec.create(
        DefaultItemData::new,
        Codec.INT.toFieldCodec("id").build(DefaultItemData::itemId),
        Codec.STRING.toFieldCodec("plain_name").build(DefaultItemData::plainName)
    ).toListCodec();

    static final Codec<List<ItemData>> CUSTOM_ITEMS_CODEC = ObjectCodec.create(
        ItemData::new,
        Codec.INT.toFieldCodec("id").build(ItemData::internalId),
        Codec.STRING.toFieldCodec("plain_name").build(ItemData::plainName),
        Base64Codec.CODEC.toFieldCodec("item_data").build(ItemData::itemData)
    ).toListCodec();

    static final Codec<StockData> STOCK_DATA_CODEC = Codec.INT.toListCodec().flatXmap(
        data -> Result.success(List.of(data.itemId(), data.amount())),
        list -> {
            if (list.size() != 2) {
                return Result.failure();
            }
            return Result.success(new StockData(list.get(0), list.get(1)));
        }
    );

    static final Codec<Map<UUID, Collection<StockData>>> STOCK_HOLDER_CODEC = UUIDCodec.UUID_AS_STRING.toMapCodecAsKey(STOCK_DATA_CODEC.toCollectionCodec());

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

    static final Codec<Map<Key, MapNode>> CUSTOM_DATA_CODEC = KEY_CODEC.toMapCodecAsKey(NodeCodec.MAP_NODE_CODEC);

    public static final Decoder<BoxData> BOX_DATA_CODEC = ObjectDecoder.create(
        BoxData::new,
        Codec.INT.xmap(MCDataVersion::dataVersion, MCDataVersion::new).toRequiredFieldDecoder("data-version"),
        USERS_CODEC.toRequiredFieldDecoder("users"),
        DEFAULT_ITEMS_CODEC.toRequiredFieldDecoder("default_items"),
        CUSTOM_ITEMS_CODEC.toRequiredFieldDecoder("custom_items"),
        STOCK_HOLDER_CODEC.toRequiredFieldDecoder("stock"),
        CUSTOM_DATA_CODEC.toRequiredFieldDecoder("custom_data")
    );
}
