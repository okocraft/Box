package net.okocraft.box.feature.command.shared;

import dev.siroshun.mcmsgdef.MessageKey;
import dev.siroshun.mcmsgdef.Placeholder;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.TabCompleter;
import org.bukkit.command.CommandSender;
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

import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

public class SharedStockListCommand {

    private static final String DEFAULT_ARGUMENT_HELP = """
        <aqua>-s <gray>(<aqua>--sorter<gray>) <aqua><sort><dark_gray> - <gray>Specifies the order of stock
        <aqua>-p <gray>(<aqua>--page<gray>) <aqua><page><dark_gray> - <gray>Specifies the page
        <aqua>-f <gray>(<aqua>--filter<gray>) <aqua><item name><dark_gray> - <gray>Filters items""";

    private static final Placeholder<Integer> CURRENT_PAGE = page -> Argument.numeric("page", page);
    private static final Placeholder<Integer> MAX_PAGE = maxPage -> Argument.numeric("max_page", maxPage);
    private static final Placeholder<Integer> NUM = num -> Argument.numeric("num", num);

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

    private final MessageKey.Arg3<String, Integer, Integer> header;
    private final MessageKey.Arg3<Integer, BoxItem, Integer> lineFormat;
    private final MessageKey notFound;
    private final MessageKey argHelp;

    public SharedStockListCommand(@NotNull DefaultMessageCollector collector) {
        this.header = MessageKey.arg3(collector.add("box.command.shared.stock-list.header", "<gray>Player <aqua><player_name><gray>'s stock list (Page <aqua><page><gray>/<aqua><max_page><gray>)"), PLAYER_NAME, CURRENT_PAGE, MAX_PAGE);
        this.lineFormat = MessageKey.arg3(collector.add("box.command.shared.stock-list.line-format", "<gray><num>. <aqua><item><gray> - <aqua><current>"), NUM, ITEM, CURRENT);
        this.notFound = MessageKey.key(collector.add("box.command.shared.stock-list.not-found", "<red>There were no items in stock matching the specified pattern."));
        this.argHelp = MessageKey.key(collector.add("box.command.shared.stock-list.arg-help", DEFAULT_ARGUMENT_HELP));
    }

    public void createAndSendStockList(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull String @Nullable [] args) {
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
            sender.sendMessage(this.notFound);
            return;
        }

        int maxPage = stockDataCollection.size() / 8 + 1;
        int page = Math.max(Math.min(context.page, maxPage), 1);

        int start = (page - 1) * 8;
        var counter = new AtomicInteger(start);

        var builder = Component.text();

        builder.append(this.header.apply(stockHolder.getName(), page, maxPage));

        stockDataCollection.stream()
            .skip(start)
            .limit(8)
            .map(stock -> this.lineFormat.apply(counter.incrementAndGet(), stock.first(), stock.secondInt()))
            .forEachOrdered(element -> builder.append(Component.newline()).append(element));

        sender.sendMessage(builder);
    }

    public @NotNull Component getArgHelp() {
        return this.argHelp.asComponent();
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
            var filter = arg.substring(1, arg.length() - 1).toLowerCase(Locale.ENGLISH);
            return item -> contains(item, filter);
        }

        if (startsWith) {
            var filter = arg.substring(0, arg.length() - 1).toLowerCase(Locale.ENGLISH);
            return item -> startsWith(item, filter);
        }

        if (endsWith) {
            var filter = arg.substring(1).toLowerCase(Locale.ENGLISH);
            return item -> endsWith(item, filter);
        }

        var filter = arg.toLowerCase(Locale.ENGLISH);
        return item -> contains(item, filter);
    }

    private static boolean startsWith(@NotNull BoxItem item, @NotNull String filter) {
        return item.getPlainName().toLowerCase(Locale.ENGLISH).startsWith(filter);
    }

    private static boolean endsWith(@NotNull BoxItem item, @NotNull String filter) {
        return item.getPlainName().toLowerCase(Locale.ENGLISH).endsWith(filter);
    }

    private static boolean contains(@NotNull BoxItem item, @NotNull String filter) {
        return item.getPlainName().toLowerCase(Locale.ENGLISH).contains(filter);
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
        }, _ -> Collections.emptyList()),
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
            return this.longArg;
        }

        private @NotNull String getShortArg() {
            return this.shortArg;
        }

        private @NotNull BiConsumer<String, Context> getContextConsumer() {
            return this.contextConsumer;
        }

        private @NotNull Function<String, List<String>> getTabCompleter() {
            return this.tabCompleter;
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
                return this.comparator.compare(item.getPlainName(), other.getPlainName());
            }

        }

        final class ByAmount implements IntComparator, Sorter {

            private final IntComparator comparator;

            private ByAmount(boolean reserved) {
                this.comparator = reserved ? IntComparators.OPPOSITE_COMPARATOR : IntComparators.NATURAL_COMPARATOR;
            }

            @Override
            public int compare(int k1, int k2) {
                return this.comparator.compare(k1, k2);
            }
        }
    }
}
