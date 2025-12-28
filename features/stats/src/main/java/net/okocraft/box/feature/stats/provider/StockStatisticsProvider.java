package net.okocraft.box.feature.stats.provider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.okocraft.box.feature.stats.database.operator.StatisticsOperators;
import net.okocraft.box.feature.stats.database.operator.StockStatisticsTableOperator;
import net.okocraft.box.feature.stats.model.StockStatistics;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@NotNullByDefault
public class StockStatisticsProvider {

    private final Cache<UUID, Int2ObjectMap<StockStatistics>> cache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(15)).build();
    private final Database database;
    private final StockStatisticsTableOperator operator;

    public StockStatisticsProvider(Database database, StatisticsOperators operators) {
        this.database = database;
        this.operator = operators.stockStatisticsTableOperator();
    }

    @SuppressWarnings("deprecation")
    public Int2ObjectMap<StockStatistics> load(UUID uuid) throws Exception {
        this.cache.invalidate(uuid);

        Int2ObjectMap<StockStatistics> statisticsByItemId = new Int2ObjectOpenHashMap<>();
        try (Connection connection = this.database.getConnection()) {
            this.operator.selectRecordsByUuid(connection, uuid, statisticsByItemId::put);
        }

        Int2ObjectMap<StockStatistics> unmodifiable = Int2ObjectMaps.unmodifiable(statisticsByItemId);
        this.cache.put(uuid, unmodifiable);

        return unmodifiable;
    }

    public @Nullable Int2ObjectMap<StockStatistics> getIfLoaded(UUID uuid) {
        return this.cache.getIfPresent(uuid);
    }

    public Int2ObjectMap<StockStatistics> getOrLoad(UUID uuid) throws Exception {
        Int2ObjectMap<StockStatistics> statisticsByItemId = this.getIfLoaded(uuid);
        if (statisticsByItemId != null) {
            return statisticsByItemId;
        }
        return this.load(uuid);
    }

    public void refresh(Set<UUID> uuids) throws SQLException {
        this.cache.invalidateAll(uuids);

        try (Connection connection = this.database.getConnection()) {
            Object2IntMap<UUID> idMap = this.database.operators().stockHolderTable().getAllStockHolderIdByUUID(connection);
            this.operator.updateTableRecordsByStockIds(connection, IntArrayList.toList(uuids.stream().filter(idMap::containsKey).mapToInt(idMap::getInt)));
        }
    }

    public void clearAllCache() {
        this.cache.invalidateAll();
    }
}
