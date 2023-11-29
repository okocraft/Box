package net.okocraft.box.compatible;

import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.util.Folia;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.compatible.item.VersionAppendingPatcherFactory;
import net.okocraft.box.compatible.paper.FoliaSchedulerWrapper;
import net.okocraft.box.core.command.CommandRegisterer;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.api.util.item.ItemVersion;
import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import net.okocraft.box.storage.api.util.item.patcher.PatcherFactory;
import net.okocraft.box.version.common.command.BukkitCommandRegisterer;
import net.okocraft.box.version.paper_1_21.Paper_1_21;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformDependent {

    public static @NotNull BoxScheduler createScheduler(@NotNull Plugin plugin) {
        if (Folia.check() ||MCDataVersion.CURRENT.isAfterOrSame(MCDataVersion.MC_1_20)) {
            return new FoliaSchedulerWrapper(plugin);
        }

        throw new UnsupportedOperationException("Unsupported version: " + Bukkit.getVersion());
    }

    public static @NotNull DefaultItemProvider createItemProvider() {
        if (true) { // TODO: MCDataVersion.MC_1_21
            return new DefaultItemProviderImpl(new ItemVersion(MCDataVersion.CURRENT, 0), Paper_1_21::defaultItems);
        }
        throw new UnsupportedOperationException("Unsupported version: " + Bukkit.getVersion());
    }

    public static @NotNull CommandRegisterer createCommandRegisterer(@NotNull String fallbackPrefix) {
        return command -> BukkitCommandRegisterer.register(fallbackPrefix, command);
    }

    private record DefaultItemProviderImpl(@NotNull ItemVersion version,
                                           @NotNull Supplier<Stream<DefaultItem>> defaultItemProvider) implements DefaultItemProvider {
        @Override
        public @NotNull Stream<DefaultItem> provide() {
            return this.defaultItemProvider.get();
        }

        @Override
        public @NotNull PatcherFactory<ItemNamePatcher> itemNamePatcherFactory() {
            return VersionAppendingPatcherFactory::createItemNamePatcher;
        }

        @Override
        public @NotNull PatcherFactory<ItemDataPatcher> itemDataPatcherFactory() {
            return VersionAppendingPatcherFactory::createItemDataPatcher;
        }
    }

    private PlatformDependent() {
        throw new UnsupportedOperationException();
    }
}
