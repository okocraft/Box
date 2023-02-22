package net.okocraft.box.feature.command.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.QuadArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.message.argument.TripleArgument;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.okocraft.box.api.message.Components.aquaItemName;
import static net.okocraft.box.api.message.Components.aquaText;
import static net.okocraft.box.api.message.Components.commandHelp;
import static net.okocraft.box.api.message.Components.grayTranslatable;
import static net.okocraft.box.api.message.Components.redTranslatable;

public final class BoxMessage {

    public static final TripleArgument<BoxItem, Integer, Integer> DEPOSIT_SUCCESS =
            (item, amount, current) -> grayTranslatable(
                    "box.command.box.deposit.success",
                    aquaItemName(item), aquaText(amount), aquaText(current)
            );

    public static final SingleArgument<Integer> DEPOSIT_ALL_SUCCESS =
            amount -> grayTranslatable("box.command.box.deposit.all-success", aquaText(amount));

    public static final Component DEPOSIT_IS_AIR = redTranslatable("box.command.box.deposit.is-air");

    public static final Component DEPOSIT_ITEM_NOT_REGISTERED =
            redTranslatable("box.command.box.deposit.item-not-registered");

    public static final Component DEPOSIT_NOT_DEPOSITED = redTranslatable("box.command.box.deposit.not-deposited");

    public static final Component DEPOSIT_NOT_FOUND = redTranslatable("box.command.box.deposit.not-found");

    public static final Component DEPOSIT_HELP_1 = commandHelp("box.command.box.deposit.help.main-hand", false);

    public static final Component DEPOSIT_HELP_2 = commandHelp("box.command.box.deposit.help.all", false);

    public static final Component DEPOSIT_HELP_3 = commandHelp("box.command.box.deposit.help.item", false);

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_SENDER =
            (targetName, item, amount, current) -> grayTranslatable(
                    "box.command.box.give.success.sender",
                    aquaText(targetName), aquaItemName(item), aquaText(amount), aquaText(current)
            );

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_TARGET =
            (senderName, item, amount, current) -> grayTranslatable(
                    "box.command.box.give.success.target",
                    aquaText(senderName), aquaItemName(item), aquaText(amount), aquaText(current)
            );

    public static final SingleArgument<BoxItem> GIVE_NO_STOCK =
            item -> redTranslatable("box.command.box.give.no-stock", aquaItemName(item));

    public static final Component GIVE_SELF = redTranslatable("box.command.box.give.self");

    public static final DoubleArgument<Player, String> GIVE_TARGET_NO_PERMISSION =
            (target, permission) -> redTranslatable(
                    "box.command.box.give.target-no-permission",
                    aquaText(target.getName()), aquaText(permission)
            );

    public static final DoubleArgument<Player, World> GIVE_TARGET_IS_IN_DISABLED_WORLD =
            (target, world) ->
                    redTranslatable(
                            "box.command.box.give.target-is-in-disabled-world",
                            aquaText(target.getName()), aquaText(world.getName())
                    );

    public static final Component GIVE_HELP = commandHelp("box.command.box.give");

    public static final TripleArgument<BoxItem, Integer, Integer> WITHDRAW_SUCCESS =
            (item, amount, current) -> grayTranslatable(
                    "box.command.box.withdraw.success",
                    aquaItemName(item), aquaText(amount), aquaText(current)
            );

    public static final TripleArgument<BoxItem, Integer, Integer> WITHDRAW_PARTIAL_SUCCESS =
            (item, amount, current) ->
                    text().append(redTranslatable("box.command.box.withdraw.stop"))
                            .append(newline())
                            .append(WITHDRAW_SUCCESS.apply(item, amount, current))
                            .build();

    public static final SingleArgument<BoxItem> WITHDRAW_NO_STOCK =
            item -> redTranslatable("box.command.box.withdraw.no-stock", aquaItemName(item));

    public static final Component WITHDRAW_INVENTORY_IS_FULL =
            redTranslatable("box.command.box.withdraw.inventory-is-full");

    public static final Component WITHDRAW_HELP = commandHelp("box.command.box.withdraw");

    public static final Component ITEM_INFO_IS_AIR = redTranslatable("box.command.box.iteminfo.is-air");

    public static final Component ITEM_INFO_NOT_REGISTERED =
            redTranslatable("box.command.box.iteminfo.item-not-registered");

    public static final SingleArgument<BoxItem> ITEM_INFO_NAME =
            item -> grayTranslatable("box.command.box.iteminfo.name", aquaItemName(item));

    public static final SingleArgument<String> ITEM_INFO_ID =
            plainName -> grayTranslatable("box.command.box.iteminfo.id", aquaText(plainName).clickEvent(ClickEvent.copyToClipboard(plainName)).hoverEvent(GeneralMessage.HOVER_TEXT_CLICK_TO_COPY));

    public static final SingleArgument<Integer> ITEM_INFO_STOCK =
            stock -> grayTranslatable("box.command.box.iteminfo.stock", aquaText(stock));

    public static final Component ITEM_INFO_HELP = commandHelp("box.command.box.iteminfo");

    public static final Component LIST_HELP =
            text().append(commandHelp("box.command.box.stocklist"))
                    .append(newline()).append(space())
                    .append(SharedMessage.stockListArgumentHelp("sorter", "s"))
                    .append(newline()).append(space())
                    .append(SharedMessage.stockListArgumentHelp("page", "p"))
                    .append(newline()).append(space())
                    .append(SharedMessage.stockListArgumentHelp("filter", "f"))
                    .build();

    private BoxMessage() {
        throw new UnsupportedOperationException();
    }
}
