package net.okocraft.box.feature.gui.api.session;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class CustomNumberHolder {

    private final AtomicInteger atomicInteger = new AtomicInteger(1);
    private Unit currentUnit = Unit.UNIT_1;

    public int getAmount() {
        return atomicInteger.get();
    }

    public void setAmount(int amount) {
        atomicInteger.set(amount);
    }

    public void increaseAmount() {
        int current = atomicInteger.addAndGet(currentUnit.getAmount());

        if (current < 1) {
            atomicInteger.set(1);
        }
    }

    public void decreaseAmount() {
        int current = atomicInteger.addAndGet(-currentUnit.getAmount());

        if (current < 1) {
            atomicInteger.set(1);
        }
    }

    public @NotNull Unit getUnit() {
        return currentUnit;
    }

    public void changeAmountUnit() {
        currentUnit = currentUnit.next();
    }

    public enum Unit {
        UNIT_1(1),
        UNIT_10(10),
        UNIT_32(32),
        UNIT_64(64),
        UNIT_128(128),
        UNIT_256(256);

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
