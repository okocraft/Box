package net.okocraft.box.feature.stats.task;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.stats.provider.ItemStatisticsProvider;
import net.okocraft.box.feature.stats.provider.StockStatisticsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNullByDefault;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@NotNullByDefault
public class StatisticsUpdateTask implements Runnable {

    private final StockStatisticsProvider stockStatisticsProvider;
    private final ItemStatisticsProvider itemStatisticsProvider;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public StatisticsUpdateTask(StockStatisticsProvider stockStatisticsProvider, ItemStatisticsProvider itemStatisticsProvider) {
        this.stockStatisticsProvider = stockStatisticsProvider;
        this.itemStatisticsProvider = itemStatisticsProvider;
    }

    @Override
    public void run() {
        Instant start = Instant.now();

        try {
            this.itemStatisticsProvider.refresh();
        } catch (Exception e) {
            BoxLogger.logger().error("Failed to refresh item statistics", e);
        }

        long tookForItemStatistics = start.until(Instant.now()).toMillis();
        start = Instant.now();

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        Set<UUID> stockUuidsToUpdate = HashSet.newHashSet(players.size());
        BoxPlayerMap playerMap = BoxAPI.api().getBoxPlayerMap();

        for (Player player : players) {
            if (playerMap.isLoaded(player)) {
                stockUuidsToUpdate.add(playerMap.get(player).getCurrentStockHolder().getUUID());
            }
        }

        if (!stockUuidsToUpdate.isEmpty()) {
            try {
                this.stockStatisticsProvider.refresh(stockUuidsToUpdate);
            } catch (SQLException e) {
                BoxLogger.logger().error("Failed to update statistics", e);
            }
        }

        long tookForStockStatistics = start.until(Instant.now()).toMillis();
        long total = tookForItemStatistics + tookForStockStatistics;
        if (3000 < total) {
            BoxLogger.logger().warn("Statistics update took {}ms ({}ms for item statistics, {}ms for stock statistics)", total, tookForItemStatistics, tookForStockStatistics);
        }
    }

    public boolean isRunning() {
        return !this.stopped.get();
    }

    public void stop() {
        this.stopped.set(true);
    }
}
