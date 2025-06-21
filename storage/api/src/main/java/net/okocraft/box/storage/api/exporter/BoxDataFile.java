package net.okocraft.box.storage.api.exporter;

import dev.siroshun.codec4j.api.error.DecodeError;
import dev.siroshun.codec4j.api.error.EncodeError;
import dev.siroshun.codec4j.io.gson.GsonIO;
import dev.siroshun.codec4j.io.gzip.GzipIO;
import dev.siroshun.event4j.api.caller.EventCaller;
import dev.siroshun.jfun.result.Result;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNullByDefault;

import java.nio.file.Path;

@NotNullByDefault
public final class BoxDataFile {

    public static Result<Void, EncodeError> encode(Path filepath, Storage storage, ItemManager itemManager, EventCaller<BoxEvent> eventCaller) {
        return GzipIO.bestCompression(GsonIO.DEFAULT).encodeTo(filepath, DataExporter.createEncoder(storage, itemManager, eventCaller), null);
    }

    public static Result<BoxData, DecodeError> decode(Path filepath) {
        return GzipIO.bestCompression(GsonIO.DEFAULT).decodeFrom(filepath, BoxData.BOX_DATA_CODEC);
    }

    private BoxDataFile() {
        throw new UnsupportedOperationException();
    }
}
