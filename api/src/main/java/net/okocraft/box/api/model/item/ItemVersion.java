package net.okocraft.box.api.model.item;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.api.util.Version;
import org.jetbrains.annotations.NotNull;

/**
 * A record that represents the item version of {@link BoxItem}s.
 *
 * @param dataVersion        the {@link MCDataVersion}
 * @param defaultItemVersion the version of the provider that generates {@link BoxDefaultItem}s
 */
public record ItemVersion(@NotNull MCDataVersion dataVersion,
                          int defaultItemVersion) implements Version<ItemVersion> {
    @Override
    public int compareTo(@NotNull ItemVersion other) {
        int i = this.dataVersion.compareTo(other.dataVersion);
        return i == 0 ? Integer.compare(this.defaultItemVersion, other.defaultItemVersion) : i;
    }
}
