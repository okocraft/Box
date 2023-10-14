package net.okocraft.box.core.model.stock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

final class Stock {

    private static final VarHandle VALUE_HANDLE;

    static {
        try {
            VALUE_HANDLE = MethodHandles.lookup().findVarHandle(Stock.class, "value", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("FieldMayBeFinal")
    private volatile int value;

    Stock(int initialValue) {
        this.value = Math.max(initialValue, 0);
    }

    int get() {
        return this.value;
    }

    int set(int value) {
        int oldValue;

        do {
            oldValue = this.value;
        } while (!VALUE_HANDLE.compareAndSet(this, oldValue, value));

        return oldValue;
    }

    @NotNull ModifyResult add(int amount) {
        int oldValue;
        int newValue;
        int excess;

        do {
            oldValue = this.value;
            newValue = oldValue + amount;

            if (0 <= newValue) {
                excess = 0;
            } else { // overflowed
                int margin = Integer.MAX_VALUE - oldValue;
                excess = amount - margin;
                newValue = Integer.MAX_VALUE;
            }
        } while (!VALUE_HANDLE.compareAndSet(this, oldValue, newValue));

        return excess == 0 ? new ModifyResult.Success(oldValue, newValue) : new ModifyResult.Overflow(oldValue, excess);
    }

    @NotNull ModifyResult subtract(int amount) {
        int oldValue;
        int newValue;

        do {
            oldValue = this.value;
            newValue = Math.max(oldValue - amount, 0);
        } while (!VALUE_HANDLE.compareAndSet(this, oldValue, newValue));

        return new ModifyResult.Success(oldValue, newValue);
    }

    @Nullable ModifyResult trySubtract(int amount) {
        int oldValue;
        int newValue;

        do {
            oldValue = this.value;

            if (oldValue < amount) {
                newValue = -1;
                break;
            }

            newValue = oldValue - amount;
        } while (!VALUE_HANDLE.compareAndSet(this, oldValue, newValue));

        return newValue != -1 ? new ModifyResult.Success(oldValue, newValue) : null;
    }

    sealed interface ModifyResult permits ModifyResult.Success, ModifyResult.Overflow {

        int oldValue();

        int newValue();

        record Success(int oldValue, int newValue) implements ModifyResult {
        }

        record Overflow(int oldValue, int excess) implements ModifyResult {
            @Override
            public int newValue() {
                return Integer.MAX_VALUE;
            }
        }
    }
}
