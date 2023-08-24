package net.okocraft.box.feature.command.shared;

import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.command.message.SharedMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
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

    public static @NotNull Component createStockList(@NotNull StockHolder stockHolder, @NotNull String @Nullable [] args) {
        var context = args != null ? createContextFromArguments(args) : new Context();

        var sorter = context.sorter;
        var filter = context.filter != null ? createFilter(context.filter) : null;

        Stream<BoxItem> stream = stockHolder.getStockedItems().stream();
        Collection<ObjectIntPair<BoxItem>> stockDataCollection;

        if (filter != null) {
            stream = stream.filter(filter);
        }

        if (sorter != null) {
            if (sorter instanceof Sorter.ByName byName) {
                stockDataCollection =
                        stream.sorted(byName)
                                .map(item -> ObjectIntPair.of(item, stockHolder.getAmount(item)))
                                .toList();
            } else if (sorter instanceof Sorter.ByAmount byAmount) {
                stockDataCollection =
                        stream.map(item -> ObjectIntPair.of(item, stockHolder.getAmount(item)))
                                .sorted((p1, p2) -> byAmount.compare(p1.rightInt(), p2.rightInt()))
                                .toList();
            } else {
                throw new UnsupportedOperationException("Unknown sorter: " + sorter);
            }
        } else {
            stockDataCollection = stream.map(item -> ObjectIntPair.of(item, stockHolder.getAmount(item))).toList();
        }

        if (stockDataCollection.isEmpty()) {
            return SharedMessage.NO_STOCK_FOUND;
        }

        int maxPage = stockDataCollection.size() / 8 + 1;
        int page = Math.max(Math.min(context.page, maxPage), 1);

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

    private static @NotNull Context createContextFromArguments(@NotNull String @NotNull [] args) {
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

    private static @NotNull Predicate<BoxItem> createFilter(@NotNull String arg) {
        boolean startsWith = arg.endsWith("*");
        boolean endsWith = arg.startsWith("*");

        if (startsWith && endsWith) {
            var filter = arg.substring(1, arg.length() - 1).toUpperCase(Locale.ROOT);
            return item -> contains(item, filter);
        }

        if (startsWith) {
            var filter = arg.substring(0, arg.length() - 1).toUpperCase(Locale.ROOT);
            return item -> startsWith(item, filter);
        }

        if (endsWith) {
            var filter = arg.substring(1).toUpperCase(Locale.ROOT);
            return item -> endsWith(item, filter);
        }

        var filter = arg.toUpperCase(Locale.ROOT);
        return item -> contains(item, filter);
    }

    private static boolean startsWith(@NotNull BoxItem item, @NotNull String filter) {
        return item.getPlainName().startsWith(filter);
    }

    private static boolean endsWith(@NotNull BoxItem item, @NotNull String filter) {
        return item.getPlainName().endsWith(filter);
    }

    private static boolean contains(@NotNull BoxItem item, @NotNull String filter) {
        return item.getPlainName().contains(filter);
    }

    private static final class Context {
        private Sorter sorter;
        private int page;
        private String filter;
    }

    private enum ArgumentType {
        SORTER(
                "sorter", "s",
                (arg, context) -> context.sorter = Sorter.get(arg.toLowerCase(Locale.ENGLISH)),
                arg -> Stream.of("na", "name-asc", "nd", "name-desc", "aa", "amount-asc", "ad", "amount-desc")
                        .filter(sorter -> sorter.startsWith(arg.toLowerCase(Locale.ENGLISH)))
                        .toList()
        ),
        PAGE("page", "p", (arg, context) -> {
            try {
                context.page = Integer.parseInt(arg);
            } catch (NumberFormatException ignored) {
            }
        }, arg -> Collections.emptyList()),
        FILTER("filter", "f", (arg, context) -> context.filter = arg, TabCompleter::itemNames);

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

    public sealed interface Sorter permits Sorter.ByName, Sorter.ByAmount {

        ByName NAME_ASC = new ByName(false);
        ByName NAME_DESC = new ByName(true);
        ByAmount AMOUNT_ASC = new ByAmount(false);
        ByAmount AMOUNT_DESC = new ByAmount(true);

        static @Nullable Sorter get(@NotNull String name) {
            return switch (name) {
                case "na", "name-asc" -> NAME_ASC;
                case "nd", "name-desc" -> NAME_DESC;
                case "aa", "amount-asc" -> AMOUNT_ASC;
                case "ad", "amount-desc" -> AMOUNT_DESC;
                default -> null;
            };
        }

        final class ByName implements Comparator<BoxItem>, Sorter {

            private final Comparator<String> comparator;

            private ByName(boolean reserved) {
                Comparator<String> comparator = String::compareTo;
                this.comparator = reserved ? comparator.reversed() : comparator;
            }

            @Override
            public int compare(BoxItem item, BoxItem other) {
                return comparator.compare(item.getPlainName(), other.getPlainName());
            }

        }

        final class ByAmount implements IntComparator, Sorter {

            private final IntComparator comparator;

            private ByAmount(boolean reserved) {
                this.comparator = reserved ? IntComparators.OPPOSITE_COMPARATOR : IntComparators.NATURAL_COMPARATOR;
            }

            @Override
            public int compare(int k1, int k2) {
                return comparator.compare(k1, k2);
            }
        }
    }
}
