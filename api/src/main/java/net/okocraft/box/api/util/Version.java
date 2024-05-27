package net.okocraft.box.api.util;

import org.jetbrains.annotations.NotNull;

/**
 * An utility interface to represent a version and compare two versions.
 * @param <V> a type that implements {@link Version}
 */
public interface Version<V extends Version<V>> extends Comparable<V> {

    /**
     * Checks if this {@link Version} is before the given {@link Version}.
     *
     * @param other the {@link Version} to compare
     * @return {@code true} if this {@link Version} is before the given {@link Version}, otherwise {@code false}
     */
    default boolean isBefore(@NotNull V other) {
        return this.compareTo(other) < 0;
    }

    /**
     * Checks if this {@link Version} is after the given {@link Version}.
     *
     * @param other the {@link Version} to compare
     * @return {@code true} if this {@link Version} is after the given {@link Version}, otherwise {@code false}
     */
    default boolean isAfter(@NotNull V other) {
        return 0 < this.compareTo(other);
    }

    /**
     * Checks if this {@link Version} is same as the given {@link Version}.
     *
     * @param other the {@link Version} to compare
     * @return {@code true} if this {@link Version} is same as the given {@link Version}, otherwise {@code false}
     */
    default boolean isSame(@NotNull V other) {
        return this.compareTo(other) == 0;
    }

    /**
     * Checks if this {@link Version} is the same as or earlier than the given {@link Version}.
     *
     * @param other the {@link Version} to compare
     * @return {@code true} if this {@link Version} is the same as or earlier than the given {@link Version}, otherwise {@code false}
     * @see #isBefore(Version)
     * @see #isSame(Version)
     */
    default boolean isBeforeOrSame(@NotNull V other) {
        return this.compareTo(other) <= 0;
    }

    /**
     * Checks if this {@link Version} is the same as or later than the given {@link Version}.
     *
     * @param other the {@link Version} to compare
     * @return {@code true} if this {@link Version} is the same as or later than the given {@link Version}, otherwise {@code false}
     * @see #isAfter(Version)
     * @see #isSame(Version)
     */
    default boolean isAfterOrSame(@NotNull V other) {
        return 0 <= this.compareTo(other);
    }

    /**
     * Checks if this {@link Version} is contained between the specified {@link Version}s.
     *
     * @param startInclusive beginning of included {@link Version}
     * @param endInclusive   end of included {@link Version}
     * @return {@code true} if this {@link Version} is contained between the specified {@link Version}s, otherwise {@code false}
     */
    default boolean isBetween(@NotNull V startInclusive, @NotNull V endInclusive) {
        return this.isAfterOrSame(startInclusive) && this.isBeforeOrSame(endInclusive);
    }

}
