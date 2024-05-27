package net.okocraft.box.feature.gui.api.session;

import org.jetbrains.annotations.NotNull;

public final class Amount {

    public static final TypedKey<Amount> SHARED_DATA_KEY = TypedKey.of(Amount.class, "shared_amount");

    private int value = 1;
    private Unit unit = Unit.UNIT_1;

    public int getValue() {
        return this.value;
    }

    public void setValue(int amount) {
        this.value = Math.max(amount, 1);
    }

    public void increase() {
        this.value += this.unit.amount;

        if (this.value < 1) {
            this.value = 1;
        }
    }

    public void decrease() {
        this.value -= this.unit.amount;

        if (this.value < 1) {
            this.value = 1;
        }
    }

    public @NotNull Unit getUnit() {
        return this.unit;
    }

    public void nextUnit() {
        this.unit = this.unit.next();
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
            return this.amount;
        }

        public @NotNull Unit next() {
            var values = values();
            var index = this.ordinal() + 1;

            if (index == values.length) {
                index = 0;
            }

            return values[index];
        }
    }

}
