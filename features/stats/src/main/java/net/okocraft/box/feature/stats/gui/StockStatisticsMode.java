package net.okocraft.box.feature.stats.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.mode.AbstractStorageMode;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.stats.model.StockStatistics;
import net.okocraft.box.feature.stats.provider.ItemStatisticsProvider;
import net.okocraft.box.feature.stats.provider.LanguageProvider;
import net.okocraft.box.feature.stats.provider.StockStatisticsProvider;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class StockStatisticsMode extends AbstractStorageMode {

    private static final TypedKey<Boolean> IS_LOADING = TypedKey.of(Boolean.class, "stock-statistics:is-loading");

    private static boolean isLoading(@NotNull PlayerSession session) {
        Boolean isLoading = session.getData(IS_LOADING);
        return isLoading != null && isLoading;
    }

    private static void setLoading(@NotNull PlayerSession session) {
        session.putData(IS_LOADING, true);
    }

    public static void setNotLoading(@NotNull PlayerSession session) {
        session.removeData(IS_LOADING);
    }

    private final LanguageProvider languageProvider;
    private final StockStatisticsProvider stockStatisticsProvider;
    private final ItemStatisticsProvider itemStatisticsProvider;

    public StockStatisticsMode(LanguageProvider languageProvider, StockStatisticsProvider stockStatisticsProvider, ItemStatisticsProvider itemStatisticsProvider) {
        this.languageProvider = languageProvider;
        this.stockStatisticsProvider = stockStatisticsProvider;
        this.itemStatisticsProvider = itemStatisticsProvider;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.KNOWLEDGE_BOOK;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull PlayerSession session) {
        return this.languageProvider.modeDisplayName().asComponent();
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        ItemEditor editor = ItemEditor.create();

        editor.displayName(item.getOriginal().effectiveName());
        editor.loreEmptyLine();

        StockHolder stockHolder = session.getSourceStockHolder();

        Int2ObjectMap<StockStatistics> statisticsByItemId = this.stockStatisticsProvider.getIfLoaded(stockHolder.getUUID());
        if (statisticsByItemId == null) {
            return editor.loreLine(this.languageProvider.guiLoading())
                .loreLine(this.languageProvider.guiClickToRefresh())
                .loreEmptyLine()
                .applyTo(session.getViewer(), item.getClonedItem());
        }

        StockStatistics statistics = statisticsByItemId.getOrDefault(item.getInternalId(), StockStatistics.EMPTY);
        if (statistics.rank() != 0) {
            editor.displayName(this.languageProvider.guiItemDisplayNameWithRank().apply(item.getOriginal().effectiveName(), statistics.rank()));
        }

        editor.loreLine(this.languageProvider.guiItemStock().apply((long) stockHolder.getAmount(item)));
        editor.loreLine(this.languageProvider.guiItemStockPercentage().apply(statistics.percentage()));

        long itemTotalAmount = this.itemStatisticsProvider.getTotalAmountByItemId(item.getInternalId());
        float itemPercentage = this.itemStatisticsProvider.calculateItemPercentage(item.getInternalId());
        editor.loreLine(this.languageProvider.guiItemStockInServer().apply(itemTotalAmount));
        editor.loreLine(this.languageProvider.guiItemStockPercentageInServer().apply(itemPercentage));

        editor.loreEmptyLine();

        return editor.applyTo(session.getViewer(), item.getClonedItem());
    }

    @Override
    public @NotNull ClickResult onSelect(@NotNull PlayerSession session) {
        boolean isLoaded = this.stockStatisticsProvider.getIfLoaded(session.getSourceStockHolder().getUUID()) != null;
        if (isLoaded) {
            return ClickResult.UPDATE_ICONS;
        }

        // To wait for loading using WaitingTask, always load the data even if isLoading is true
        return this.scheduleLoading(session);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        boolean isLoaded = this.stockStatisticsProvider.getIfLoaded(session.getSourceStockHolder().getUUID()) != null;
        if (isLoaded) {
            // When the statistics are already loaded, update a clicked button only
            return ClickResult.UPDATE_BUTTON;
        } else if (isLoading(session)) {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        return this.scheduleLoading(session);
    }

    @Override
    public boolean hasAdditionalButton() {
        return true;
    }

    @Override
    public @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot) {
        return new Button() {
            @Override
            public int getSlot() {
                return slot;
            }

            @Override
            public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
                ItemEditor editor = ItemEditor.create();

                editor.displayName(StockStatisticsMode.this.languageProvider.guiGlobalDisplayName());
                editor.loreEmptyLine();

                long globalStock = StockStatisticsMode.this.itemStatisticsProvider.getTotalAmount();
                editor.loreLine(StockStatisticsMode.this.languageProvider.guiGlobalStock().apply(globalStock));

                editor.loreEmptyLine();

                return editor.createItem(session.getViewer(), Material.NETHER_STAR);
            }

            @Override
            public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
                return ClickResult.NO_UPDATE_NEEDED;
            }
        };
    }

    @Override
    public boolean canUse(@NotNull PlayerSession session) {
        return session.getViewer().hasPermission("box.stats");
    }

    private ClickResult.WaitingTask scheduleLoading(@NotNull PlayerSession session) {
        setLoading(session);

        ClickResult.WaitingTask waitingTask = ClickResult.waitingTask();
        BoxAPI.api().getScheduler().runAsyncTask(() -> {
            UUID uuid = session.getSourceStockHolder().getUUID();
            try {
                this.stockStatisticsProvider.load(uuid);
                setNotLoading(session); // If an exception occurs during loading, the menu will continue to display “Loading...” and will not attempt to reload within the session
            } catch (Exception e) {
                BoxLogger.logger().error("Failed to load stock statistics for {}", uuid, e);
            }

            waitingTask.complete(ClickResult.UPDATE_ICONS);
        });
        return waitingTask;
    }
}
