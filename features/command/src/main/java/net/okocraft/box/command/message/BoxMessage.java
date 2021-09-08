package net.okocraft.box.command.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.QuadArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.message.argument.TripleArgument;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class BoxMessage {

    public static final TripleArgument<BoxItem, Integer, Integer> DEPOSIT_SUCCESS =
            (item, amount, current) ->
                    translatable()
                            .key("box.command.box.deposit.success")
                            .args(
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(amount, AQUA),
                                    text(current, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final SingleArgument<Integer> DEPOSIT_ALL_SUCCESS =
            amount -> translatable()
                    .key("box.command.box.deposit.all-success")
                    .args(text(amount, AQUA))
                    .color(GRAY)
                    .build();

    public static final Component DEPOSIT_IS_AIR =
            translatable("box.command.box.deposit.is-air", RED);

    public static final Component DEPOSIT_ITEM_NOT_REGISTERED =
            translatable("box.command.box.deposit.item-not-registered", RED);

    public static final Component DEPOSIT_NOT_DEPOSITED =
            translatable("box.command.box.deposit.not-deposited", RED);

    public static final Component DEPOSIT_NOT_FOUND =
            translatable("box.command.box.deposit.not-found", RED);

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_SENDER =
            (targetName, item, amount, current) ->
                    translatable()
                            .key("box.command.box.give.success.sender")
                            .args(
                                    text(targetName, AQUA),
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(amount, AQUA),
                                    text(current, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_TARGET =
            (senderName, item, amount, current) ->
                    translatable()
                            .key("box.command.box.give.success.target")
                            .args(
                                    text(senderName, AQUA),
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(amount, AQUA),
                                    text(current, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final SingleArgument<BoxItem> GIVE_NO_STOCK =
            item ->
                    translatable()
                            .key("box.command.box.give.no-stock")
                            .args(item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()))
                            .color(RED)
                            .build();

    public static final DoubleArgument<Player, String> GIVE_TARGET_NO_PERMISSION =
            (target, permission) ->
                    translatable()
                            .key("box.command.box.give.target-no-permission")
                            .args(text(target.getName(), AQUA), text(permission, AQUA))
                            .color(RED)
                            .build();

    public static final TripleArgument<BoxItem, Integer, Integer> WITHDRAW_SUCCESS =
            (item, amount, current) ->
                    translatable()
                            .key("box.command.box.withdraw.success")
                            .args(
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(amount, AQUA),
                                    text(current, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final TripleArgument<BoxItem, Integer, Integer> WITHDRAW_PARTIAL_SUCCESS =
            (item, amount, current) ->
                    translatable("box.command.box.withdraw.stop", RED)
                            .append(newline())
                            .append(WITHDRAW_SUCCESS.apply(item, amount, current));

    public static final SingleArgument<BoxItem> WITHDRAW_NO_STOCK =
            item ->
                    translatable()
                            .key("box.command.box.withdraw.no-stock")
                            .args(item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()))
                            .color(RED)
                            .build();

    public static final Component WITHDRAW_INVENTORY_IS_FULL =
            translatable("box.command.box.withdraw.inventory-is-full", RED);

    private BoxMessage() {
        throw new UnsupportedOperationException();
    }
}
