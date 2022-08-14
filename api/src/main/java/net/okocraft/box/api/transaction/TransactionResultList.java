package net.okocraft.box.api.transaction;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/**
 * A class to indicate a list of transaction results.
 */
public class TransactionResultList {

    /**
     * Creates a new {@link TransactionResultList}.
     *
     * @param type the {@link TransactionResultType} whose {@link TransactionResultType#isModified()} is false
     * @return a new {@link TransactionResultList}
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TransactionResultList create(@NotNull TransactionResultType type) {
        Objects.requireNonNull(type);

        if (!type.isModified()) {
            return new TransactionResultList(type, null);
        } else {
            throw new IllegalArgumentException(
                    "A type whose TransactionResultType#isModified is true cannot be used for this method"
            );
        }
    }

    /**
     * Creates a new {@link TransactionResultList}.
     *
     * @param type       the {@link TransactionResultType} whose {@link TransactionResultType#isModified()} is true
     * @param resultList the {@link TransactionResult} list
     * @return a new {@link TransactionResultList}
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull TransactionResultList create(@NotNull TransactionResultType type,
                                                        @NotNull List<TransactionResult> resultList) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(resultList);

        if (type.isModified()) {
            return new TransactionResultList(type, resultList);
        } else {
            throw new IllegalArgumentException(
                    "A type whose TransactionResultType#isModified is false cannot be used for this method"
            );
        }
    }

    private final TransactionResultType type;
    private final List<TransactionResult> resultList;

    private TransactionResultList(@NotNull TransactionResultType type, @Nullable List<TransactionResult> resultList) {
        this.type = type;
        this.resultList = type.isModified() ? Objects.requireNonNull(resultList) : null;
    }

    /**
     * Gets the transaction result type.
     *
     * @return the {@link TransactionResultType}
     */
    public @NotNull TransactionResultType getType() {
        return type;
    }

    /**
     * Gets the {@link TransactionResult} list.
     *
     * @return the {@link TransactionResult} list
     * @throws IllegalStateException the {@link TransactionResultType#isModified()} of {@link #getType()} is false
     */
    public @NotNull @Unmodifiable List<TransactionResult> getResultList() {
        if (resultList != null) {
            return resultList;
        } else {
            throw new IllegalStateException(
                    "Could not get the item because TransactionResultType#isModified is false"
            );
        }
    }
}
