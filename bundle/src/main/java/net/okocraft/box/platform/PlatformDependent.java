package net.okocraft.box.platform;

import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.util.Folia;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.core.command.CommandRegisterer;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.api.util.item.ItemVersion;
import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import net.okocraft.box.storage.api.util.item.patcher.PatcherFactory;
import net.okocraft.box.version.common.command.BukkitCommandRegisterer;
import net.okocraft.box.version.common.scheduler.FoliaSchedulerWrapper;
import net.okocraft.box.version.paper_1_21.Paper_1_21;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformDependent {

    public static @NotNull BoxScheduler createScheduler(@NotNull Plugin plugin) throws NotSupportedException {
        if (Folia.check() || MCDataVersion.CURRENT.isAfterOrSame(MCDataVersion.MC_1_20)) {
            return new FoliaSchedulerWrapper(plugin);
        }

        throw new NotSupportedException("Unsupported version: " + Bukkit.getVersion());
    }

    public static @NotNull DefaultItemProvider createItemProvider() throws NotSupportedException {
        if (MCDataVersion.CURRENT.isAfterOrSame(MCDataVersion.MC_1_20_4)) { // TODO: back to MC_1_21 after Minecraft 1.21 released
            return new DefaultItemProviderImpl(new ItemVersion(MCDataVersion.MC_1_20_4, 0), Paper_1_21::defaultItems);
        }
        throw new NotSupportedException("Unsupported version: " + Bukkit.getVersion());
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
            return PatcherFactories::createItemNamePatcher;
        }

        @Override
        public @NotNull PatcherFactory<ItemDataPatcher> itemDataPatcherFactory() {
            return PatcherFactories::createItemDataPatcher;
        }
    }

    private PlatformDependent() {
        throw new UnsupportedOperationException();
    }

    public static final class NotSupportedException extends Exception {
        public final String reason;
        private NotSupportedException(@NotNull String reason) {
            super(reason, null, true, false);
            this.reason = reason;
        }
    }
}
