package net.okocraft.box.compatible;

import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.util.Folia;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.compatible.paper.FoliaSchedulerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class PlatformDependent {

    public static @NotNull BoxScheduler createScheduler(@NotNull Plugin plugin) {
        if (Folia.check() || MCDataVersion.MC_1_20.isAfterOrSame(MCDataVersion.CURRENT)) {
            return new FoliaSchedulerWrapper(plugin);
        }

        throw new UnsupportedOperationException("Unsupported version: " + Bukkit.getVersion());
    }

    private PlatformDependent() {
        throw new UnsupportedOperationException();
    }
}
