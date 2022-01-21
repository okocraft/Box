package net.okocraft.box.feature.notifier.factory;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.okocraft.box.api.message.Components.aquaText;
import static net.okocraft.box.api.message.Components.redText;
import static net.okocraft.box.api.message.Components.whiteText;

public class NotificationFactory {

    private static final Component COMMON_PARTS_1 = text(" - ", DARK_GRAY);
    private static final Component COMMON_PARTS_2 = text(" (", DARK_GRAY);
    private static final Component COMMON_PARTS_3 = text(")", DARK_GRAY);

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull NotificationFactory create(@NotNull BoxItem item) {
        return new NotificationFactory(item);
    }

    private final BoxItem item;
    private int current;
    private int diff;

    private NotificationFactory(@NotNull BoxItem item) {
        this.item = item;
    }

    @Contract("_ -> this")
    public @NotNull NotificationFactory current(int current) {
        this.current = current;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull NotificationFactory increments(int increments) {
        this.diff += increments;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull NotificationFactory decrements(int decrements) {
        this.diff -= decrements;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull NotificationFactory previous(int previous) {
        this.diff += current - previous;
        return this;
    }

    public @NotNull Component build() {
        var notification =
                translatable()
                        .key(item.getOriginal())
                        .append(COMMON_PARTS_1)
                        .append(whiteText(current));

        if (diff != 0) {
            notification.append(COMMON_PARTS_2)
                    .append(0 <= diff ? aquaText("+" + diff) : redText(diff))
                    .append(COMMON_PARTS_3);
        }

        return notification.build();
    }
}
