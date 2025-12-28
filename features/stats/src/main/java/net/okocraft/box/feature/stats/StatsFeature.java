package net.okocraft.box.feature.stats;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.player.PlayerCollectItemInfoEvent;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.feature.stats.database.operator.StatisticsOperators;
import net.okocraft.box.feature.stats.gui.StockStatisticsMode;
import net.okocraft.box.feature.stats.listener.BoxEventHandlers;
import net.okocraft.box.feature.stats.provider.ItemStatisticsProvider;
import net.okocraft.box.feature.stats.provider.LanguageProvider;
import net.okocraft.box.feature.stats.provider.StockStatisticsProvider;
import net.okocraft.box.feature.stats.task.StatisticsUpdateTask;
import net.okocraft.box.storage.api.holder.StorageHolder;
import net.okocraft.box.storage.implementation.database.DatabaseStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.Duration;

public class StatsFeature extends AbstractBoxFeature {

    private static final Key LISTENER_KEY = Key.key("box", "stats");

    private final LanguageProvider languageProvider;

    private StockStatisticsProvider stockStatisticsProvider;
    private ItemStatisticsProvider itemStatisticsProvider;
    private StockStatisticsMode stockStatisticsMode;
    private StatisticsUpdateTask updateTask;

    public StatsFeature(@NotNull FeatureContext.Registration context) {
        super("stats");
        this.languageProvider = new LanguageProvider(context.defaultMessageCollector());
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        if (!StorageHolder.isInitialized() || !(StorageHolder.getStorage() instanceof DatabaseStorage storage)) {
            BoxLogger.logger().warn("Stats feature is disabled because storage is not loaded yet or is not a database storage.");
            return;
        }

        Database database = storage.getDatabase();
        StatisticsOperators operators = StatisticsOperators.create(database);

        try {
            operators.initTables(database);
        } catch (SQLException e) {
            BoxLogger.logger().error("Failed to initialize the statistics tables", e);
            return;
        }

        this.stockStatisticsProvider = new StockStatisticsProvider(database, operators);
        this.itemStatisticsProvider = new ItemStatisticsProvider(database, operators);

        try {
            this.itemStatisticsProvider.refresh();
        } catch (Exception e) {
            BoxLogger.logger().error("Failed to refresh item statistics", e);
            return;
        }

        this.stockStatisticsMode = new StockStatisticsMode(this.languageProvider, this.stockStatisticsProvider, this.itemStatisticsProvider);
        ClickModeRegistry.register(this.stockStatisticsMode);

        this.updateTask = new StatisticsUpdateTask(this.stockStatisticsProvider, this.itemStatisticsProvider);
        BoxAPI.api().getScheduler().scheduleRepeatingAsyncTask(this.updateTask, Duration.ofMinutes(1), this.updateTask::isRunning);

        BoxAPI.api().getListenerSubscriber().subscribe(PlayerCollectItemInfoEvent.class, LISTENER_KEY, event -> BoxEventHandlers.onPlayerCollectItemInfoCollect(event, this.languageProvider, this.stockStatisticsProvider, this.itemStatisticsProvider));
    }

    @Override
    public void disable(FeatureContext.@NotNull Disabling context) {
        BoxAPI.api().getListenerSubscriber().unsubscribeByKey(LISTENER_KEY);

        if (this.updateTask != null) {
            this.updateTask.stop();
        }

        if (this.stockStatisticsMode != null) {
            ClickModeRegistry.unregister(this.stockStatisticsMode);
        }

        if (this.stockStatisticsProvider != null) {
            this.stockStatisticsProvider.clearAllCache();
        }

        if (this.itemStatisticsProvider != null) {
            this.itemStatisticsProvider.clearCache();
        }
    }
}
