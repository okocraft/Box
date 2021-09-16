package net.okocraft.box.feature.gui.api.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class TransactionAmountHolder {

    private static final Map<UUID, Unit> UNIT_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, AtomicInteger> TRANSACTION_AMOUNT_MAP = new ConcurrentHashMap<>();

    public static int getAmount(@NotNull Player player) {
        return Optional.ofNullable(TRANSACTION_AMOUNT_MAP.get(player.getUniqueId()))
                .map(AtomicInteger::get)
                .orElse(1);
    }

    public static void set(@NotNull Player player, int amount) {
        getOrCreate(player).set(amount);
    }

    public static void reset(@NotNull Player player) {
        getOrCreate(player).set(1);
    }

    public static void increase(@NotNull Player player, @NotNull Unit unit) {
        var integer = getOrCreate(player);
        var current = integer.addAndGet(unit.getAmount());

        if (current < 1) {
            current = 1;
            integer.set(current);
        }
    }

    public static void decrease(@NotNull Player player, @NotNull Unit unit) {
        var integer = getOrCreate(player);
        var current = integer.addAndGet(-unit.getAmount());

        if (current < 1) {
            current = 1;
            integer.set(current);
        }
    }

    public static @NotNull Unit getUnit(@NotNull Player player) {
        return UNIT_MAP.getOrDefault(player.getUniqueId(), Unit.UNIT_1);
    }

    public static void changeUnit(@NotNull Player player) {
        UNIT_MAP.put(player.getUniqueId(), getUnit(player).next());
    }

    private static @NotNull AtomicInteger getOrCreate(@NotNull Player player) {
        return TRANSACTION_AMOUNT_MAP.computeIfAbsent(player.getUniqueId(), k -> new AtomicInteger(1));
    }

    public enum Unit {
        UNIT_1(1),
        UNIT_10(10),
        UNIT_32(32),
        UNIT_64(64),
        UNIT_128(128),
        UNIT_256(256);

        private static final int MAX = Unit.values().length;

        private final int amount;

        Unit(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public @NotNull Unit next() {
            var values = values();
            var index = ordinal() + 1;

            if (index == values.length) {
                index = 0;
            }

            return values[index];
        }
    }
}
