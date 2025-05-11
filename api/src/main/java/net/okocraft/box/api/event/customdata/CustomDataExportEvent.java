package net.okocraft.box.api.event.customdata;

import dev.siroshun.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

@NotNullByDefault
public class CustomDataExportEvent extends BoxEvent implements Cancellable {

    private final Key key;
    private final MapNode node;
    private @Nullable MapNode resultNode;
    private boolean cancel;

    public CustomDataExportEvent(Key key, MapNode node) {
        this.key = key;
        this.node = node.asView();
    }

    public Key getKey() {
        return this.key;
    }

    public @NotNull MapNode getOriginalNode() {
        return this.node;
    }

    public @NotNull MapNode getResultNode() {
        return Objects.requireNonNullElse(this.resultNode, this.node);
    }

    public void setResultNode(@NotNull MapNode node) {
        this.resultNode = node;
    }

    public void editNode(@NotNull Consumer<MapNode> editor) {
        MapNode node = this.getResultNode().copy();
        editor.accept(node);
        this.resultNode = node;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
