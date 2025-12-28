package net.okocraft.box.feature.stats.listener;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.okocraft.box.api.event.player.PlayerCollectItemInfoEvent;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.stats.model.StockStatistics;
import net.okocraft.box.feature.stats.provider.ItemStatisticsProvider;
import net.okocraft.box.feature.stats.provider.LanguageProvider;
import net.okocraft.box.feature.stats.provider.StockStatisticsProvider;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public final class BoxEventHandlers {

    public static void onPlayerCollectItemInfoCollect(PlayerCollectItemInfoEvent event, LanguageProvider languageProvider, StockStatisticsProvider stockStatisticsProvider, ItemStatisticsProvider itemStatisticsProvider) {
        BoxPlayer player = event.getBoxPlayer();

        if (!player.getPlayer().hasPermission("box.stats")) {
            return;
        }

        StockHolder stockHolder = player.getCurrentStockHolder();
        Int2ObjectMap<StockStatistics> statisticsByItemId;
        try {
            statisticsByItemId = stockStatisticsProvider.getOrLoad(stockHolder.getUUID());
        } catch (Exception e) {
            BoxLogger.logger().error("Failed to load stock statistics for {}", stockHolder.getUUID(), e);
            return;
        }

        int itemId = event.getItem().getInternalId();
        StockStatistics statistics = statisticsByItemId.get(itemId);
        if (statistics.rank() != 0) {
            event.addInfo(languageProvider.commandBoxItemInfoStockPercentage().apply(statistics.percentage(), statistics.rank()));
        }

        long itemTotalAmount = itemStatisticsProvider.getTotalAmountByItemId(itemId);
        float itemPercentage = itemStatisticsProvider.calculateItemPercentage(itemId);
        event.addInfo(languageProvider.commandBoxItemInfoStockInServer().apply(itemTotalAmount, itemPercentage));
    }
}
