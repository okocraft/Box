package net.okocraft.box.api.model.item;

import net.okocraft.box.api.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;

/**
 * A record that represents the item version of {@link BoxItem}s.
 *
 * @param dataVersion        the {@link MCDataVersion}
 * @param defaultItemVersion the version of the provider that generates {@link BoxDefaultItem}s
 */
public record ItemVersion(@NotNull MCDataVersion dataVersion,
                          int defaultItemVersion) implements Comparable<ItemVersion> {

    /**
     * Checks if this {@link ItemVersion} is after the specified {@link ItemVersion}.
     *
     * @param other the {@link ItemVersion} to compare
     * @return {@code true} if this {@link ItemVersion} is after the specified {@link ItemVersion}, otherwise {@code false}
     */
    public boolean isAfter(@NotNull ItemVersion other) {
        return 0 < this.compareTo(other);
    }

    /**
     * Checks if this {@link ItemVersion} is same as the specified {@link ItemVersion}.
     *
     * @param other the {@link ItemVersion} to compare
     * @return {@code true} if this {@link ItemVersion} is same as the specified {@link ItemVersion}, otherwise {@code false}
     */
    public boolean isSame(@NotNull ItemVersion other) {
        return 0 == this.compareTo(other);
    }

    /**
     * Checks if this {@link ItemVersion} is before the specified {@link ItemVersion}.
     *
     * @param other the {@link ItemVersion} to compare
     * @return {@code true} if this {@link ItemVersion} is before the specified {@link ItemVersion}, otherwise {@code false}
     */
    public boolean isBefore(@NotNull ItemVersion other) {
        return 0 > this.compareTo(other);
    }

    @Override
    public int compareTo(@NotNull ItemVersion other) {
        if (this.dataVersion.isSame(other.dataVersion)) {
            return Integer.compare(this.defaultItemVersion, other.defaultItemVersion);
        }

        return this.dataVersion.isAfter(other.dataVersion) ? 1 : -1;
    }
}
