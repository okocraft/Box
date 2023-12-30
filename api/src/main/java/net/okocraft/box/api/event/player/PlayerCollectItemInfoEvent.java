package net.okocraft.box.api.event.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A {@link PlayerEvent} called when the {@link BoxPlayer} collects item info through {@code /box iteminfo}.
 */
public class PlayerCollectItemInfoEvent extends PlayerEvent {

    private final BoxItem item;
    private final List<Component> info = new ArrayList<>();

    /**
     * The constructor of a {@link PlayerCollectItemInfoEvent}.
     *
     * @param boxPlayer the player of this event
     * @param item      the {@link BoxItem} to collect information
     */
    public PlayerCollectItemInfoEvent(@NotNull BoxPlayer boxPlayer, @NotNull BoxItem item) {
        super(boxPlayer);
        this.item = Objects.requireNonNull(item);
    }

    /**
     * Gets the {@link BoxItem} to collect information.
     *
     * @return the {@link BoxItem} to collect information
     */
    public @NotNull BoxItem getItem() {
        return this.item;
    }

    /**
     * Returns the list of information of the {@link BoxItem} for the {@link BoxPlayer}.
     *
     * @return the list of information of the {@link BoxItem} for the {@link BoxPlayer}
     */
    public @NotNull @UnmodifiableView List<Component> getInfo() {
        return Collections.unmodifiableList(this.info);
    }

    /**
     * Adds information of the {@link BoxItem}.
     *
     * @param component the {@link Component} to represents information of the {@link BoxItem}
     */
    public void addInfo(@NotNull Component component) {
        this.info.add(component);
    }

    @Override
    public @NotNull String toDebugLog() {
        return "PlayerCollectItemInfoEvent{" +
                "uuid=" + this.getBoxPlayer().getUUID() +
                ", name=" + this.getBoxPlayer().getName() +
                ", item=" + this.getItem().getPlainName() +
                ", info=" + this.getInfo().stream().map(PlainTextComponentSerializer.plainText()::serialize).toList() +
                '}';
    }

    @Override
    public String toString() {
        return "PlayerCollectItemInfoEvent{" +
                "boxPlayer=" + this.getBoxPlayer() +
                ", item=" + this.getItem() +
                ", info=" + this.getInfo() +
                '}';
    }
}
