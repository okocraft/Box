package net.okocraft.box.feature.command.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.TripleArgument;
import net.okocraft.box.api.model.stock.StockData;

import static net.okocraft.box.api.message.Components.aquaItemName;
import static net.okocraft.box.api.message.Components.aquaText;
import static net.okocraft.box.api.message.Components.aquaTranslatable;
import static net.okocraft.box.api.message.Components.grayText;
import static net.okocraft.box.api.message.Components.grayTranslatable;
import static net.okocraft.box.api.message.Components.redTranslatable;

public final class SharedMessage {

    public static final TripleArgument<String, Integer, Integer> STOCK_LIST_HEADER =
            (target, page, maxPage) -> grayTranslatable(
                    "box.command.shared.stock-list.header",
                    aquaText(target), aquaText(page), aquaText(maxPage)
            );

    public static final DoubleArgument<Integer, StockData> STOCK_LIST_ITEM_AMOUNT =
            (num, stockData) -> grayTranslatable(
                    "box.command.shared.stock-list.amount",
                    grayText(num), aquaItemName(stockData.item()), aquaText(stockData.amount())
            );

    public static final Component NO_STOCK_FOUND = redTranslatable("box.command.shared.stock-list.no-stock-found");

    public static Component stockListArgumentHelp(String arg, String shortArg) {
        var keyPrefix = "box.command.shared.stock-list.help.argument." + arg;
        return grayTranslatable(
                keyPrefix + ".format",
                aquaText("-" + shortArg), aquaText("--" + arg), aquaTranslatable(keyPrefix + ".value")
        );
    }
}
