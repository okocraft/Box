package net.okocraft.box.feature.command.boxadmin.stock;

import dev.siroshun.mcmsgdef.MessageKey;
import dev.siroshun.mcmsgdef.Placeholder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

class StockTransferCommand extends AbstractCommand {

    private static final String CONFIRM = "confirm";
    private static final String CANCEL = "cancel";
    private static final long REQUEST_TIMEOUT_NANOS = TimeUnit.MINUTES.toNanos(1);

    private static final Placeholder<String> FROM = name -> Argument.string("from", name);
    private static final Placeholder<String> TO = name -> Argument.string("to", name);
    private static final Placeholder<Integer> COUNT = count -> Argument.numeric("count", count);

    private static final String DEFAULT_CONFIRMATION_MESSAGE = """
        <gray>Transfer all stock from <aqua><from><gray> to <aqua><to><gray>.
        <red>This operation cannot be undone.
        <gray>To confirm, run <green>/boxadmin stock transfer confirm
        <gray>To cancel, run <red>/boxadmin stock transfer cancel""";

    private final Map<CommandSender, Request> confirmationMap = new ConcurrentHashMap<>();

    private final MessageKey.Arg2<String, String> confirmation;
    private final MessageKey cancel;
    private final MessageKey expired;
    private final MessageKey samePlayer;
    private final MessageKey.Arg1<String> noStock;
    private final MessageKey.Arg3<String, String, Integer> successSender;
    private final MessageKey.Arg1<Integer> remaining;
    private final MessageKey.Arg2<String, String> successFrom;
    private final MessageKey.Arg2<String, String> successTo;
    private final MessageKey help;

    StockTransferCommand(@NotNull DefaultMessageCollector collector) {
        super("transfer", "box.admin.command.stock.transfer", Set.of("tr"));
        this.confirmation = MessageKey.arg2(collector.add("box.command.boxadmin.stock.transfer.confirmation", DEFAULT_CONFIRMATION_MESSAGE), FROM, TO);
        this.cancel = MessageKey.key(collector.add("box.command.boxadmin.stock.transfer.cancel", "<gray>Cancelled transfer operation."));
        this.expired = MessageKey.key(collector.add("box.command.boxadmin.stock.transfer.expired", "<red>The transfer request has expired. Please run the command again."));
        this.samePlayer = MessageKey.key(collector.add("box.command.boxadmin.stock.transfer.same-player", "<red>The source and destination players are the same."));
        this.noStock = MessageKey.arg1(collector.add("box.command.boxadmin.stock.transfer.no-stock", "<aqua><from><gray> has no stock to transfer."), FROM);
        this.successSender = MessageKey.arg3(collector.add("box.command.boxadmin.stock.transfer.success.sender", "<gray>Transferred <aqua><count><gray> kinds of items from <aqua><from><gray> to <aqua><to><gray>."), FROM, TO, COUNT);
        this.remaining = MessageKey.arg1(collector.add("box.command.boxadmin.stock.transfer.remaining", "<yellow><count><gray> kinds of items were partially left in the source because the destination stock reached the limit."), COUNT);
        this.successFrom = MessageKey.arg2(collector.add("box.command.boxadmin.stock.transfer.success.from", "<gray>Your Box stock has been transferred to <aqua><to><gray> by <aqua><player_name><gray>."), PLAYER_NAME, TO);
        this.successTo = MessageKey.arg2(collector.add("box.command.boxadmin.stock.transfer.success.to", "<aqua><from><gray>'s Box stock has been transferred to you by <aqua><player_name><gray>."), PLAYER_NAME, FROM);
        this.help = MessageKey.key(collector.add("box.command.boxadmin.stock.transfer.help", "<aqua>/boxadmin stock transfer <from> <to><dark_gray> - <gray>Transfers all stock to another player"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        if (args.length == 3 && (CONFIRM.equalsIgnoreCase(args[2]) || CANCEL.equalsIgnoreCase(args[2]))) {
            Request request = this.confirmationMap.remove(sender);

            if (request != null) {
                if (CANCEL.equalsIgnoreCase(args[2])) {
                    sender.sendMessage(this.cancel);
                } else if (request.isExpired()) {
                    sender.sendMessage(this.expired);
                } else {
                    this.transfer(sender, request);
                }
                return;
            }
        }

        if (args.length < 4) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        BoxUser from = UserSearcher.search(args[2]);
        if (from == null) {
            sender.sendMessage(ErrorMessages.PLAYER_NOT_FOUND.apply(args[2]));
            return;
        }

        BoxUser to = UserSearcher.search(args[3]);
        if (to == null) {
            sender.sendMessage(ErrorMessages.PLAYER_NOT_FOUND.apply(args[3]));
            return;
        }

        if (from.getUUID().equals(to.getUUID())) {
            sender.sendMessage(this.samePlayer);
            return;
        }

        this.confirmationMap.put(sender, new Request(from, to, System.nanoTime()));
        sender.sendMessage(this.confirmation.apply(nameOf(from), nameOf(to)));
    }

    private void transfer(@NotNull CommandSender sender, @NotNull Request request) {
        String fromName = nameOf(request.from());
        String toName = nameOf(request.to());

        StockHolder fromHolder = BoxAPI.api().getStockManager().getPersonalStockHolder(request.from());
        StockHolder toHolder = BoxAPI.api().getStockManager().getPersonalStockHolder(request.to());

        StockActor actor = StockActor.fromSender(sender);
        Result result = transferAll(
            fromHolder, toHolder,
            new CommandCauses.AdminTransferOut(request.to(), actor),
            new CommandCauses.AdminTransferIn(request.from(), actor),
            new CommandCauses.AdminTransferRefund(request.to(), actor)
        );

        if (result.transferred() == 0 && result.remaining() == 0) {
            sender.sendMessage(this.noStock.apply(fromName));
            return;
        }

        if (result.transferred() > 0) {
            sender.sendMessage(this.successSender.apply(fromName, toName, result.transferred()));
        }

        if (result.remaining() > 0) {
            sender.sendMessage(this.remaining.apply(result.remaining()));
        }

        if (result.transferred() == 0) {
            return;
        }

        Player fromPlayer = Bukkit.getPlayer(request.from().getUUID());
        if (fromPlayer != null && sender != fromPlayer) {
            fromPlayer.sendMessage(this.successFrom.apply(sender.getName(), toName));
        }

        Player toPlayer = Bukkit.getPlayer(request.to().getUUID());
        if (toPlayer != null && sender != toPlayer) {
            toPlayer.sendMessage(this.successTo.apply(sender.getName(), fromName));
        }
    }

    /**
     * Moves all stock from {@code from} to {@code to}.
     * <p>
     * The amount that would exceed {@link Integer#MAX_VALUE} in {@code to} is left in {@code from}.
     * If the destination cannot accept the decreased amount (e.g. its stock grew concurrently, or increasing failed),
     * the amount is returned to {@code from} with {@code refundCause}.
     *
     * @param from        the source stock holder
     * @param to          the destination stock holder
     * @param outCause    the cause for decreasing the source stock
     * @param inCause     the cause for increasing the destination stock
     * @param refundCause the cause for returning the amount that could not be added to the destination
     * @return the number of item kinds that were transferred and that were (partially) left in the source
     */
    @VisibleForTesting
    static @NotNull Result transferAll(@NotNull StockHolder from, @NotNull StockHolder to,
                                       @NotNull CommandCauses.AdminTransferOut outCause,
                                       @NotNull CommandCauses.AdminTransferIn inCause,
                                       @NotNull CommandCauses.AdminTransferRefund refundCause) {
        int transferred = 0;
        int remaining = 0;

        for (BoxItem item : from.getStockedItems()) {
            int amount = from.getAmount(item);

            if (amount <= 0) {
                continue;
            }

            int capacity = Integer.MAX_VALUE - to.getAmount(item);
            int moved = capacity > 0 ? from.decreaseToZero(item, Math.min(amount, capacity), outCause) : 0;
            int added = 0;

            if (moved > 0) {
                try {
                    added = to.increaseToMax(item, moved, inCause);
                } finally {
                    if (added < moved) {
                        from.increase(item, moved - added, refundCause);
                    }
                }
            }

            if (added > 0) {
                transferred++;
            }

            if (capacity < amount || added < moved) {
                remaining++;
            }
        }

        return new Result(transferred, remaining);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 3) {
            List<String> players = TabCompleter.players(args[2]);

            if (!this.confirmationMap.containsKey(sender)) {
                return players;
            }

            String arg = args[2].toLowerCase(Locale.ROOT);
            return Stream.concat(Stream.of(CONFIRM, CANCEL).filter(s -> s.startsWith(arg)), players.stream()).toList();
        }

        if (args.length == 4) {
            return TabCompleter.players(args[3]);
        }

        return Collections.emptyList();
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }

    private static @NotNull String nameOf(@NotNull BoxUser user) {
        return user.getName().orElseGet(user.getUUID()::toString);
    }

    private record Request(@NotNull BoxUser from, @NotNull BoxUser to, long createdAt) {
        private boolean isExpired() {
            return REQUEST_TIMEOUT_NANOS < System.nanoTime() - this.createdAt;
        }
    }

    @VisibleForTesting
    record Result(int transferred, int remaining) {
    }
}
