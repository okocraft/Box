package net.okocraft.box.feature.command.boxadmin.stock;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import net.okocraft.box.feature.command.util.TabCompleter;
import net.okocraft.box.feature.command.util.UserStockHolderOperator;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class StockListCommand extends AbstractCommand {

    private static final Map<String, ArgumentType> ARGUMENT_MAP;

    static {
        var map = new HashMap<String, ArgumentType>();

        for (var arg : ArgumentType.values()) {
            map.put("--" + arg.getLongArg(), arg);
            map.put("-" + arg.getShortArg(), arg);
        }

        ARGUMENT_MAP = Collections.unmodifiableMap(map);
    }

    public StockListCommand() {
        super("list", "box.admin.command.stock.list", Set.of("l"));
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.STOCK_LIST_HELP;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(getHelp());
            return;
        }

        var targetStockHolder = UserStockHolderOperator.create(args[2]).supportOffline(true).getUserStockHolder();

        if (targetStockHolder == null) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[2]));
            return;
        }

        var context = new Context();

        if (3 < args.length) {
            ArgumentType type = null;
            for (var arg : Arrays.copyOfRange(args, 3, args.length)) {
                if (type != null) {
                    type.getContextConsumer().accept(arg, context);
                    type = null;
                } else {
                    type = ARGUMENT_MAP.get(arg.toLowerCase(Locale.ENGLISH));
                }
            }
        }

        var stockDataCollection = targetStockHolder.toStockDataCollection();

        var sorter = context.getSorter();
        var filter = context.getFilter();

        if (sorter != null || (filter != null && !filter.isEmpty())) {
            var stream = stockDataCollection.stream();

            if (sorter != null) {
                stream = stream.sorted(sorter);
            }

            if (filter != null && !filter.isEmpty()) {
                stream = stream.filter(stock -> stock.item().getPlainName().startsWith(filter));
            }

            stockDataCollection = stream.toList();
        }

        int maxPage = stockDataCollection.size() / 8 + 1;
        int page = Math.max(Math.min(context.getPage(), maxPage), 1);

        int start = (page - 1) * 8;
        var counter = new AtomicInteger(start);

        var result =
                stockDataCollection.stream()
                        .skip(start)
                        .limit(8)
                        .map(stock -> BoxAdminMessage.STOCK_LIST_ITEM_AMOUNT.apply(counter.incrementAndGet(), stock))
                        .toList();

        sender.sendMessage(
                BoxAdminMessage.STOCK_LIST_HEADER
                        .apply(targetStockHolder, page, maxPage)
                        .append(Component.newline())
                        .append(Component.join(JoinConfiguration.separator(Component.newline()), result))
        );
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            return Collections.emptyList();
        }

        if (args.length == 3) {
            return TabCompleter.players(args[2]);
        }

        if (args.length == 4) {
            return List.copyOf(ARGUMENT_MAP.keySet());
        }

        int index = args.length - 1;
        var type = ARGUMENT_MAP.get(args[index - 1]);

        if (type != null) {
            return type.getTabCompleter().apply(args[index]);
        } else {
            return List.copyOf(ARGUMENT_MAP.keySet());
        }
    }

    private static class Context {

        private static final Comparator<StockData> NAME_ASC = Comparator.comparing(stock -> stock.item().getPlainName());
        private static final Comparator<StockData> NAME_DESC = NAME_ASC.reversed();
        private static final Comparator<StockData> AMOUNT_ASC = Comparator.comparing(StockData::amount);
        private static final Comparator<StockData> AMOUNT_DESC = AMOUNT_ASC.reversed();

        private Comparator<StockData> sorter;
        private int page;
        private String filter;

        private Context() {
        }

        public @Nullable Comparator<StockData> getSorter() {
            return sorter;
        }

        public void setSorter(Comparator<StockData> sorter) {
            this.sorter = sorter;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public @Nullable String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }
    }

    private enum ArgumentType {
        SORTER(
                "sorter", "s",
                (arg, context) -> {
                    context.setSorter(switch (arg.toLowerCase(Locale.ROOT)) {
                        case "na", "name-asc" -> Context.NAME_ASC;
                        case "nd", "name-desc" -> Context.NAME_DESC;
                        case "aa", "amount-asc" -> Context.AMOUNT_ASC;
                        case "ad", "amount-desc" -> Context.AMOUNT_DESC;
                        default -> null;
                    });
                },
                arg -> {
                    return Stream.of("na", "name-asc", "nd", "name-desc", "aa", "amount-asc", "ad", "amount-desc")
                            .filter(sorter -> sorter.startsWith(arg.toLowerCase(Locale.ENGLISH)))
                            .toList();
                }
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

        public @NotNull String getLongArg() {
            return longArg;
        }

        public @NotNull String getShortArg() {
            return shortArg;
        }

        public @NotNull BiConsumer<String, Context> getContextConsumer() {
            return contextConsumer;
        }

        public @NotNull Function<String, List<String>> getTabCompleter() {
            return tabCompleter;
        }
    }
}
