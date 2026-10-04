package net.okocraft.box.api.event.customdata;

import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Called synchronously before an export reads storage data.
 * Listeners can register operations that persist their in-memory data.
 */
@NotNullByDefault
public final class DataExportPrepareEvent extends BoxEvent {

    private final List<Preparation> preparations = new ArrayList<>();

    /** Creates an export preparation event. */
    public DataExportPrepareEvent() {
    }

    /**
     * Registers an operation to complete before exporting.
     *
     * @param preparation the operation
     */
    public void addPreparation(Preparation preparation) {
        this.preparations.add(Objects.requireNonNull(preparation));
    }

    /**
     * Runs registered operations in order after listeners have been called.
     *
     * @throws Exception if preparing data fails
     */
    public void prepare() throws Exception {
        for (Preparation preparation : List.copyOf(this.preparations)) {
            preparation.run();
        }
    }

    /** An operation that can report a persistence failure to the exporter. */
    @FunctionalInterface
    public interface Preparation {
        /**
         * Prepares data for export.
         *
         * @throws Exception if preparing data fails
         */
        void run() throws Exception;
    }
}
