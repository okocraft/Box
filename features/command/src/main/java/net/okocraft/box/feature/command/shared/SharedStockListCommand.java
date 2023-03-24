package net.okocraft.box.feature.command.shared;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.command.message.SharedMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SharedStockListCommand {

    private static final Map<String, ArgumentType> ARGUMENT_MAP;
    private static final List<String> ARGUMENT_TYPES;

    static {
        var map = new HashMap<String, ArgumentType>();

        for (var arg : ArgumentType.values()) {
            map.put("--" + arg.getLongArg(), arg);
            map.put("-" + arg.getShortArg(), arg);
        }

        ARGUMENT_MAP = Collections.unmodifiableMap(map);
        ARGUMENT_TYPES = List.copyOf(ARGUMENT_MAP.keySet());
    }

    public static @NotNull Context createContextFromArguments(@NotNull String @NotNull [] args) {
        var context = new Context();

        ArgumentType type = null;

        for (var arg : args) {
            if (type != null) {
                type.getContextConsumer().accept(arg, context);
                type = null;
            } else {
                type = ARGUMENT_MAP.get(arg.toLowerCase(Locale.ENGLISH));
            }
        }

        return context;
    }

    public static @NotNull Component createStockList(@NotNull StockHolder stockHolder, @NotNull Context context) {
        var sorter = context.getSorter();
        var filter = context.getFilter();

        var stockDataStream = stockHolder.stockDataStream();

        if (sorter != null || (filter != null && !filter.isEmpty())) {

            if (sorter != null) {
                stockDataStream = stockDataStream.sorted(sorter);
            }

            if (filter != null && !filter.isEmpty()) {
                stockDataStream = stockDataStream.filter(createFilter(filter));
            }
        }

        var stockDataCollection = stockDataStream.toList();

        if (stockDataCollection.isEmpty()) {
            return SharedMessage.NO_STOCK_FOUND;
        }

        int maxPage = stockDataCollection.size() / 8 + 1;
        int page = Math.max(Math.min(context.getPage(), maxPage), 1);

        int start = (page - 1) * 8;
        var counter = new AtomicInteger(start);

        var result = Component.text().append(SharedMessage.STOCK_LIST_HEADER.apply(stockHolder.getName(), page, maxPage));

        stockDataCollection.stream()
                .skip(start)
                .limit(8)
                .map(stock -> SharedMessage.STOCK_LIST_ITEM_AMOUNT.apply(counter.incrementAndGet(), stock))
                .forEachOrdered(element -> result.append(Component.newline()).append(element));

        return result.build();
    }

    public static @NotNull List<String> getArgumentTypes() {
        return ARGUMENT_TYPES;
    }

    public static @NotNull List<String> createTabCompletion(@NotNull String previousArg, @NotNull String currentArg) {
        var type = ARGUMENT_MAP.get(previousArg);

        if (type != null) {
            return type.getTabCompleter().apply(currentArg);
        } else {
            return List.copyOf(ARGUMENT_MAP.keySet());
        }
    }

    private static @NotNull Predicate<StockData> createFilter(@NotNull String arg) {
        boolean startsWith = arg.endsWith("*");
        boolean endsWith = arg.startsWith("*");

        if (startsWith && endsWith) {
            var filter = arg.substring(1, arg.length() - 1).toUpperCase(Locale.ROOT);
            return stockData -> contains(stockData, filter);
        }

        if (startsWith) {
            var filter = arg.substring(0, arg.length() - 1).toUpperCase(Locale.ROOT);
            return stockData -> startsWith(stockData, filter);
        }

        if (endsWith) {
            var filter = arg.substring(1).toUpperCase(Locale.ROOT);
            return stockData -> endsWith(stockData, filter);
        }

        var filter = arg.toUpperCase(Locale.ROOT);
        return stockData -> contains(stockData, filter);
    }

    private static boolean startsWith(@NotNull StockData stockData, @NotNull String filter) {
        return stockData.item().getPlainName().startsWith(filter);
    }

    private static boolean endsWith(@NotNull StockData stockData, @NotNull String filter) {
        return stockData.item().getPlainName().endsWith(filter);
    }

    private static boolean contains(@NotNull StockData stockData, @NotNull String filter) {
        return stockData.item().getPlainName().contains(filter);
    }

    public static final class Context {

        private static final Comparator<StockData> NAME_ASC = Comparator.comparing(stock -> stock.item().getPlainName());
        private static final Comparator<StockData> NAME_DESC = NAME_ASC.reversed();
        private static final Comparator<StockData> AMOUNT_ASC = Comparator.comparing(StockData::amount);
        private static final Comparator<StockData> AMOUNT_DESC = AMOUNT_ASC.reversed();

        private Comparator<StockData> sorter;
        private int page;
        private String filter;

        public Context() {
        }

        public @Nullable Comparator<StockData> getSorter() {
            return sorter;
        }

        public int getPage() {
            return page;
        }

        private @Nullable String getFilter() {
            return filter;
        }

        private void setSorter(Comparator<StockData> sorter) {
            this.sorter = sorter;
        }

        private void setPage(int page) {
            this.page = page;
        }

        private void setFilter(String filter) {
            this.filter = filter;
        }
    }

    private enum ArgumentType {
        SORTER(
                "sorter", "s",
                (arg, context) -> context.setSorter(switch (arg.toLowerCase(Locale.ROOT)) {
                    case "na", "name-asc" -> Context.NAME_ASC;
                    case "nd", "name-desc" -> Context.NAME_DESC;
                    case "aa", "amount-asc" -> Context.AMOUNT_ASC;
                    case "ad", "amount-desc" -> Context.AMOUNT_DESC;
                    default -> null;
                }),
                arg -> Stream.of("na", "name-asc", "nd", "name-desc", "aa", "amount-asc", "ad", "amount-desc")
                        .filter(sorter -> sorter.startsWith(arg.toLowerCase(Locale.ENGLISH)))
                        .toList()
        ),
        PAGE("page", "p", (arg, context) -> {
            try {
                context.setPage(Integer.parseInt(arg));
            } catch (NumberFormatException ignored) {
            }
        }, arg -> Collections.emptyList()),
        FILTER("filter", "f", (arg, context) -> context.setFilter(arg), TabCompleter::itemNames);

        private final String longArg;
        private final String shortArg;
        private final BiConsumer<String, Context> contextConsumer;
        private final Function<String, List<String>> tabCompleter;

        ArgumentType(@NotNull String longArg, @NotNull String shortArg, @NotNull BiConsumer<String, Context> contextConsumer,
                     @NotNull Function<String, List<String>> tabCompleter) {
            this.longArg = longArg;
            this.shortArg = shortArg;
            this.contextConsumer = contextConsumer;
            this.tabCompleter = tabCompleter;
        }

        private @NotNull String getLongArg() {
            return longArg;
        }

        private @NotNull String getShortArg() {
            return shortArg;
        }

        private @NotNull BiConsumer<String, Context> getContextConsumer() {
            return contextConsumer;
        }

        private @NotNull Function<String, List<String>> getTabCompleter() {
            return tabCompleter;
        }
    }
}
