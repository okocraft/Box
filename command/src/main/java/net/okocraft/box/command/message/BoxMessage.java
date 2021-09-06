package net.okocraft.box.command.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.message.argument.TripleArgument;
import net.okocraft.box.api.model.item.BoxItem;

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

    private BoxMessage() {
        throw new UnsupportedOperationException();
    }
}
