package version.paper_1_21;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.version.common.item.ItemSources;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@VersionSpecific
public final class Paper_1_21 {

    @VersionSpecific.Version
    public static final MCDataVersion VERSION = MCDataVersion.MC_1_21;

    @VersionSpecific.DefaultItemSource
    public static @NotNull Stream<DefaultItem> defaultItems() {
        return new ItemSources.Merger()
                .append(ItemSources.itemTypes(registry(RegistryKey.ITEM)))
                .append(ItemSources.potions(registry(RegistryKey.POTION)))
                .append(ItemSources.enchantedBooks(registry(RegistryKey.ENCHANTMENT)))
                .append(ItemSources.fireworks())
                .append(ItemSources.goatHorns(registry(RegistryKey.INSTRUMENT)))
                .append(ItemSources.ominousBottles())
                .result();
    }

    private static <T extends Keyed> @NotNull Registry<T> registry(@NotNull RegistryKey<T> key) {
        return RegistryAccess.registryAccess().getRegistry(key);
    }

    private Paper_1_21() {
        throw new UnsupportedOperationException();
    }
}
