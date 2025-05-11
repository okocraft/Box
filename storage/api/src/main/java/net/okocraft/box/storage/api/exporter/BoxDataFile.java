package net.okocraft.box.storage.api.exporter;

import dev.siroshun.codec4j.api.error.DecodeError;
import dev.siroshun.codec4j.api.error.EncodeError;
import dev.siroshun.codec4j.api.file.DefaultOpenOptions;
import dev.siroshun.codec4j.io.gson.GsonIO;
import dev.siroshun.event4j.api.caller.EventCaller;
import dev.siroshun.jfun.result.Result;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@NotNullByDefault
public final class BoxDataFile {

    public static Result<Void, EncodeError> encode(Path filepath, Storage storage, ItemManager itemManager, EventCaller<BoxEvent> eventCaller) {
        try (var out = Files.newOutputStream(filepath, DefaultOpenOptions.fileOpenOptions());
             var gzipOut = new GZIPOutputStream(out)) {
            return GsonIO.DEFAULT.encodeTo(gzipOut, DataExporter.createEncoder(storage, itemManager, eventCaller), null);
        } catch (IOException e) {
            return EncodeError.fatalError(e).asFailure();
        }
    }

    public static Result<BoxData, DecodeError> decode(Path filepath) {
        try (var in = Files.newInputStream(filepath);
             var gzipIn = new GZIPInputStream(in)) {
            return GsonIO.DEFAULT.decodeFrom(gzipIn, BoxData.BOX_DATA_CODEC);
        } catch (IOException e) {
            return DecodeError.fatalError(e).asFailure();
        }
    }

    private BoxDataFile() {
        throw new UnsupportedOperationException();
    }
}
