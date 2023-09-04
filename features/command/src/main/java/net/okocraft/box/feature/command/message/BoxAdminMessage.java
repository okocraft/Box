package net.okocraft.box.feature.command.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.QuadArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.message.argument.TripleArgument;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.user.BoxUser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.okocraft.box.api.message.Components.aquaBoxUser;
import static net.okocraft.box.api.message.Components.aquaItemName;
import static net.okocraft.box.api.message.Components.aquaText;
import static net.okocraft.box.api.message.Components.commandHelp;
import static net.okocraft.box.api.message.Components.grayTranslatable;
import static net.okocraft.box.api.message.Components.greenTranslatable;
import static net.okocraft.box.api.message.Components.redTranslatable;
import static net.okocraft.box.api.message.Components.whiteText;

public final class BoxAdminMessage {

    public static final SingleArgument<String> VERSION_INFO =
            version ->
                    grayTranslatable(
                            "box.command.boxadmin.version.info",
                            aquaText(version).clickEvent(ClickEvent.copyToClipboard(version)).hoverEvent(GeneralMessage.HOVER_TEXT_CLICK_TO_COPY)
                    );

    public static final Component VERSION_HELP = commandHelp("box.command.boxadmin.version");

    public static final Component REGISTER_IS_AIR = redTranslatable("box.command.boxadmin.register.is-air");

    public static final SingleArgument<ItemStack> REGISTER_ALREADY_REGISTERED =
            item -> redTranslatable("box.command.boxadmin.register.already-registered", aquaItemName(item));

    public static final SingleArgument<BoxCustomItem> REGISTER_SUCCESS =
            item -> grayTranslatable(
                    "box.command.boxadmin.register.success",
                    aquaItemName(item),
                    aquaText(item.getPlainName()).clickEvent(ClickEvent.copyToClipboard(item.getPlainName())).hoverEvent(GeneralMessage.HOVER_TEXT_CLICK_TO_COPY)
            );

    public static final Component REGISTER_TIP_RENAME = grayTranslatable("box.command.boxadmin.register.tip-rename");

    public static final SingleArgument<Throwable> REGISTER_FAILURE =
            throwable -> redTranslatable("box.command.boxadmin.register.failure", whiteText(throwable.getMessage()));

    public static final Component REGISTER_HELP = commandHelp("box.command.boxadmin.register");

    public static final Component RELOAD_START = grayTranslatable("box.command.boxadmin.reload.start");

    public static final Component RELOAD_FINISH = grayTranslatable("box.command.boxadmin.reload.finish");

    public static final Component RELOAD_HELP = commandHelp("box.command.boxadmin.reload");

    public static final SingleArgument<BoxItem> RENAME_IS_NOT_CUSTOM_ITEM =
            item -> redTranslatable("box.command.boxadmin.rename.is-not-custom-item", aquaItemName(item));

    public static final SingleArgument<String> RENAME_ALREADY_USED_NAME =
            name -> redTranslatable("box.command.boxadmin.rename.already-used-name", aquaText(name));

    public static final SingleArgument<BoxItem> RENAME_SUCCESS =
            item -> grayTranslatable("box.command.boxadmin.rename.success", aquaItemName(item));

    public static final SingleArgument<Throwable> RENAME_FAILURE =
            throwable -> redTranslatable("box.command.boxadmin.rename.failure", whiteText(throwable.getMessage()));

    public static final Component RENAME_HELP = commandHelp("box.command.boxadmin.rename");

    public static final Component INFINITY_MODE_ENABLE = greenTranslatable("box.command.boxadmin.infinity.enabled");

    public static final Component INFINITY_MODE_DISABLED = redTranslatable("box.command.boxadmin.infinity.disabled");

    public static final SingleArgument<Boolean> INFINITY_MODE_TOGGLE =
            enabled -> grayTranslatable(
                    "box.command.boxadmin.infinity.toggle",
                    enabled ? INFINITY_MODE_ENABLE : INFINITY_MODE_DISABLED
            );

    public static final DoubleArgument<Player, Boolean> INFINITY_MODE_TOGGLE_SENDER =
            (target, enabled) -> grayTranslatable(
                    "box.command.boxadmin.infinity.toggle-sender",
                    aquaText(target.getName()),
                    enabled ? INFINITY_MODE_ENABLE : INFINITY_MODE_DISABLED
            );

    public static final DoubleArgument<CommandSender, Boolean> INFINITY_MODE_TOGGLE_TARGET =
            (sender, enabled) -> grayTranslatable(
                    "box.command.boxadmin.infinity.toggle-target",
                    aquaText(sender.getName()),
                    enabled ? INFINITY_MODE_ENABLE : INFINITY_MODE_DISABLED
            );

    public static final Component INFINITY_MODE_TIP = grayTranslatable("box.command.boxadmin.infinity.tip");

    public static final Component INFINITY_HELP = commandHelp("box.command.boxadmin.infinity");

    public static final SingleArgument<BoxUser> RESET_ALL_SUCCESS_SENDER =
            target -> grayTranslatable("box.command.boxadmin.resetall.success.sender", aquaBoxUser(target));

    public static final SingleArgument<CommandSender> RESET_ALL_SUCCESS_TARGET =
            sender -> grayTranslatable("box.command.boxadmin.resetall.success.target", aquaText(sender.getName()));

    public static final Component RESET_ALL_CANCEL = grayTranslatable("box.command.boxadmin.resetall.cancel");

    public static final SingleArgument<BoxUser> RESET_ALL_CONFIRMATION =
            target ->
                    text().append(grayTranslatable("box.command.boxadmin.resetall.confirmation.info", aquaBoxUser(target)))
                            .append(newline())
                            .append(grayTranslatable("box.command.boxadmin.resetall.confirmation.warning"))
                            .append(newline())
                            .append(confirmationCommand("confirm"))
                            .append(newline())
                            .append(confirmationCommand("cancel"))
                            .build();

    private static Component confirmationCommand(String arg) {
        return grayTranslatable(
                "box.command.boxadmin.resetall.confirmation." + arg + "-command",
                aquaText("/boxadmin resetall " + arg)
        );
    }

    public static final Component RESET_ALL_HELP = commandHelp("box.command.boxadmin.resetall");

    public static final Component STOCK_HELP = commandHelp("box.command.boxadmin.stock");

    public static final QuadArgument<BoxUser, BoxItem, Integer, Integer> STOCK_GIVE_SUCCESS_SENDER =
            (target, item, increments, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.give.success.sender",
                            aquaBoxUser(target), aquaItemName(item),
                            aquaText(increments), aquaText(currentAmount)
                    );

    public static final QuadArgument<String, BoxItem, Integer, Integer> STOCK_GIVE_SUCCESS_TARGET =
            (senderName, item, increments, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.give.success.target",
                            aquaText(senderName), aquaItemName(item),
                            aquaText(increments), aquaText(currentAmount)
                    );

    public static final Component STOCK_GIVE_HELP = commandHelp("box.command.boxadmin.stock.give");

    public static final Component STOCK_INFO_HELP = commandHelp("box.command.boxadmin.stock.info");

    public static final TripleArgument<BoxUser, BoxItem, Integer> STOCK_INFO_AMOUNT =
            (target, item, amount) -> grayTranslatable(
                    "box.command.boxadmin.stock.info.amount",
                    aquaBoxUser(target), aquaItemName(item), aquaText(amount)
            );

    public static final Component STOCK_LIST_HELP =
            text().append(commandHelp("box.command.boxadmin.stock.list"))
                    .append(newline()).append(space())
                    .append(SharedMessage.stockListArgumentHelp("sorter", "s"))
                    .append(newline()).append(space())
                    .append(SharedMessage.stockListArgumentHelp("page", "p"))
                    .append(newline()).append(space())
                    .append(SharedMessage.stockListArgumentHelp("filter", "f"))
                    .build();

    public static final DoubleArgument<BoxUser, BoxItem> STOCK_RESET_SUCCESS_SENDER =
            (target, item) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.reset.success.sender",
                            aquaBoxUser(target), aquaItemName(item)
                    );

    public static final DoubleArgument<String, BoxItem> STOCK_RESET_SUCCESS_TARGET =
            (senderName, item) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.reset.success.target",
                            aquaText(senderName), aquaItemName(item)
                    );

    public static final Component STOCK_RESET_HELP = commandHelp("box.command.boxadmin.stock.reset");

    public static final TripleArgument<BoxUser, BoxItem, Integer> STOCK_SET_SUCCESS_SENDER =
            (target, item, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.set.success.sender",
                            aquaBoxUser(target), aquaItemName(item), aquaText(currentAmount)
                    );

    public static final TripleArgument<String, BoxItem, Integer> STOCK_SET_SUCCESS_TARGET =
            (senderName, item, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.set.success.target",
                            aquaText(senderName), aquaItemName(item), aquaText(currentAmount)
                    );

    public static final Component STOCK_SET_HELP = commandHelp("box.command.boxadmin.stock.set");

    public static final QuadArgument<BoxUser, BoxItem, Integer, Integer> STOCK_TAKE_SUCCESS_SENDER =
            (target, item, increments, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.take.success.sender",
                            aquaBoxUser(target), aquaItemName(item),
                            aquaText(increments), aquaText(currentAmount)
                    );

    public static final QuadArgument<String, BoxItem, Integer, Integer> STOCK_TAKE_SUCCESS_TARGET =
            (senderName, item, increments, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.take.success.target",
                            aquaText(senderName), aquaItemName(item),
                            aquaText(increments), aquaText(currentAmount)
                    );

    public static final Component STOCK_TAKE_HELP = commandHelp("box.command.boxadmin.stock.take");

    private BoxAdminMessage() {
        throw new UnsupportedOperationException();
    }
}
