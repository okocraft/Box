package net.okocraft.box.feature.command.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.QuadArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.message.argument.TripleArgument;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.okocraft.box.api.message.Components.aquaItemName;
import static net.okocraft.box.api.message.Components.aquaText;
import static net.okocraft.box.api.message.Components.aquaTranslatable;
import static net.okocraft.box.api.message.Components.commandHelp;
import static net.okocraft.box.api.message.Components.grayText;
import static net.okocraft.box.api.message.Components.grayTranslatable;
import static net.okocraft.box.api.message.Components.greenTranslatable;
import static net.okocraft.box.api.message.Components.redTranslatable;
import static net.okocraft.box.api.message.Components.whiteText;

public final class BoxAdminMessage {

    public static final SingleArgument<String> VERSION_INFO =
            version ->
                    grayTranslatable(
                            "box.command.boxadmin.version.info",
                            aquaText(version)
                    );

    public static final Component VERSION_HELP = commandHelp("box.command.boxadmin.version");

    public static final Component REGISTER_IS_AIR = redTranslatable("box.command.boxadmin.register.is-air");

    public static final SingleArgument<ItemStack> REGISTER_ALREADY_REGISTERED =
            item -> redTranslatable("box.command.boxadmin.register.already-registered", aquaItemName(item));

    public static final SingleArgument<BoxCustomItem> REGISTER_SUCCESS =
            item -> grayTranslatable(
                    "box.command.boxadmin.register.success",
                    aquaItemName(item),
                    aquaText(item.getPlainName()).clickEvent(ClickEvent.copyToClipboard(item.getPlainName()))
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

    public static final SingleArgument<UserStockHolder> RESET_SUCCESS_SENDER =
            target -> grayTranslatable("box.command.boxadmin.reset.success.sender", aquaText(target.getName()));

    public static final SingleArgument<CommandSender> RESET_SUCCESS_TARGET =
            sender -> grayTranslatable("box.command.boxadmin.reset.success.target", aquaText(sender.getName()));

    public static final Component RESET_CANCEL = grayTranslatable("box.command.boxadmin.reset.cancel");

    public static final SingleArgument<UserStockHolder> RESET_CONFIRMATION =
            target ->
                    text().append(grayTranslatable(
                                    "box.command.boxadmin.reset.confirmation.info", aquaText(target.getName())
                            )).append(newline())
                            .append(grayTranslatable("box.command.boxadmin.reset.confirmation.warning"))
                            .append(newline())
                            .append(confirmationCommand("confirm"))
                            .append(newline())
                            .append(confirmationCommand("cancel"))
                            .build();

    private static Component confirmationCommand(String arg) {
        return grayTranslatable(
                "box.command.boxadmin.reset.confirmation." + arg + "-command",
                aquaText("/boxadmin reset " + arg)
        );
    }

    public static final Component RESET_HELP = commandHelp("box.command.boxadmin.reset");

    public static final Component STOCK_HELP = commandHelp("box.command.boxadmin.stock");

    public static final QuadArgument<String, BoxItem, Integer, Integer> STOCK_GIVE_SUCCESS_SENDER =
            (targetName, item, increments, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.give.success.sender",
                            aquaText(targetName), aquaItemName(item),
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

    public static final TripleArgument<String, BoxItem, Integer> STOCK_INFO_AMOUNT =
            (targetName, item, amount) -> grayTranslatable(
                    "box.command.boxadmin.stock.info.amount",
                    aquaText(targetName), aquaItemName(item), aquaText(amount)
            );

    public static final Component STOCK_LIST_HELP =
            text().append(commandHelp("box.command.boxadmin.stock.list"))
                    .append(newline()).append(space())
                    .append(stockListArgumentHelp("sorter", "s"))
                    .append(newline()).append(space())
                    .append(stockListArgumentHelp("page", "p"))
                    .append(newline()).append(space())
                    .append(stockListArgumentHelp("filter", "f"))
                    .build();

    private static Component stockListArgumentHelp(String arg, String shortArg) {
        var keyPrefix = "box.command.boxadmin.stock.list.help.argument." + arg;
        return grayTranslatable(
                keyPrefix + ".format",
                aquaText("-" + shortArg), aquaText("--" + arg), aquaTranslatable(keyPrefix + ".value")
        );
    }

    public static final TripleArgument<UserStockHolder, Integer, Integer> STOCK_LIST_HEADER =
            (target, page, maxPage) -> grayTranslatable(
                    "box.command.boxadmin.stock.list.header",
                    aquaText(target.getName()), aquaText(page), aquaText(maxPage)
            );

    public static final DoubleArgument<Integer, StockData> STOCK_LIST_ITEM_AMOUNT =
            (num, stockData) -> grayTranslatable(
                    "box.command.boxadmin.stock.list.amount",
                    grayText(num), aquaItemName(stockData.item()), aquaText(stockData.amount())
            );

    public static final TripleArgument<String, BoxItem, Integer> STOCK_SET_SUCCESS_SENDER =
            (targetName, item, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.set.success.sender",
                            aquaText(targetName), aquaItemName(item), aquaText(currentAmount)
                    );

    public static final TripleArgument<String, BoxItem, Integer> STOCK_SET_SUCCESS_TARGET =
            (senderName, item, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.set.success.target",
                            aquaText(senderName), aquaItemName(item), aquaText(currentAmount)
                    );

    public static final Component STOCK_SET_HELP = commandHelp("box.command.boxadmin.stock.set");

    public static final QuadArgument<String, BoxItem, Integer, Integer> STOCK_TAKE_SUCCESS_SENDER =
            (targetName, item, increments, currentAmount) ->
                    grayTranslatable(
                            "box.command.boxadmin.stock.take.success.sender",
                            aquaText(targetName), aquaItemName(item),
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
