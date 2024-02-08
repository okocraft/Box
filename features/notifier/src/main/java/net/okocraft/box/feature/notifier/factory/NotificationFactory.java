package net.okocraft.box.feature.notifier.factory;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.user.BoxUser;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class NotificationFactory {

    private static final Component COMMON_PARTS_1 = text(" - ", DARK_GRAY);
    private static final Component COMMON_PARTS_2 = text(" (", DARK_GRAY);
    private static final Component COMMON_PARTS_3 = text(")", DARK_GRAY);

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull NotificationFactory create(@NotNull StockEvent event) {
        return new NotificationFactory(event.getItem(), event.getAmount());
    }

    private final BoxItem item;
    private final int current;
    private int diff;

    private NotificationFactory(@NotNull BoxItem item, int current) {
        this.item = item;
        this.current = current;
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
        this.diff += this.current - previous;
        return this;
    }

    public void showActionBar(@NotNull BoxUser target) {
        var player = Bukkit.getPlayer(target.getUUID());

        if (player != null) {
            player.sendActionBar(this.createNotification());
        }
    }

    private @NotNull Component createNotification() {
        var builder =
                translatable()
                        .key(this.item.getOriginal())
                        .append(COMMON_PARTS_1)
                        .append(text(this.current, WHITE));

        if (this.diff != 0) {
            builder.append(COMMON_PARTS_2)
                    .append(0 <= this.diff ? text("+" + this.diff, AQUA) : text(this.diff, RED))
                    .append(COMMON_PARTS_3);
        }

        return builder.build();
    }
}
