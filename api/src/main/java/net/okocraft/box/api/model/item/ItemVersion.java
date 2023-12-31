package net.okocraft.box.api.model.item;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.api.util.Version;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A record that represents the item version of {@link BoxItem}s.
 *
 * @param dataVersion        the {@link MCDataVersion}
 * @param defaultItemVersion the version of the provider that generates {@link BoxDefaultItem}s
 */
public record ItemVersion(@NotNull MCDataVersion dataVersion,
                          int defaultItemVersion) implements Version<ItemVersion> {

    /**
     * Creates an {@link ItemVersion} with the specified data version.
     *
     * @param dataVersion the {@link MCDataVersion}
     * @return a new {@link ItemVersion} with the specified data version
     */
    public static @NotNull ItemVersion of(int dataVersion) {
        return of(dataVersion, 0);
    }

    /**
     * Creates an {@link ItemVersion} with the specified data version and default item version.
     *
     * @param dataVersion        the {@link MCDataVersion}
     * @param defaultItemVersion the version of the provider that generates {@link BoxDefaultItem}s
     * @return a new {@link ItemVersion} with the specified data version default item version
     */
    public static @NotNull ItemVersion of(int dataVersion, int defaultItemVersion) {
        return new ItemVersion(MCDataVersion.of(dataVersion), defaultItemVersion);
    }

    /**
     * The constructor of {@link ItemVersion}.
     *
     * @param dataVersion        the {@link MCDataVersion}
     * @param defaultItemVersion the version of the provider that generates {@link BoxDefaultItem}s
     */
    public ItemVersion {
        Objects.requireNonNull(dataVersion);
    }

    @Override
    public int compareTo(@NotNull ItemVersion other) {
        int i = this.dataVersion.compareTo(other.dataVersion);
        return i == 0 ? Integer.compare(this.defaultItemVersion, other.defaultItemVersion) : i;
    }
}
