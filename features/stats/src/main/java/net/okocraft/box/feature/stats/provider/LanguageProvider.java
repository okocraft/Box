package net.okocraft.box.feature.stats.provider;

import dev.siroshun.mcmsgdef.MessageKey;
import dev.siroshun.mcmsgdef.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public class LanguageProvider {

    private static final Placeholder<Long> AMOUNT_PLACEHOLDER = amount -> Argument.string("amount", String.format("%,d", amount));
    private static final Placeholder<Float> PERCENTAGE_PLACEHOLDER = percentage -> Argument.string("percentage", String.format("%.2f", percentage));
    private static final Placeholder<Integer> RANK_PLACEHOLDER = rank -> Argument.numeric("rank", rank);

    private final MessageKey modeDisplayName;
    private final MessageKey guiLoading;
    private final MessageKey guiClickToRefresh;
    private final MessageKey.Arg2<Component, Integer> guiItemDisplayNameWithRank;
    private final MessageKey.Arg1<Long> guiItemStock;
    private final MessageKey.Arg1<Float> guiItemStockPercentage;
    private final MessageKey.Arg1<Long> guiItemStockInServer;
    private final MessageKey.Arg1<Float> guiItemStockPercentageInServer;
    private final MessageKey guiGlobalDisplayName;
    private final MessageKey.Arg1<Long> guiGlobalStock;
    private final MessageKey.Arg2<Float, Integer> commandBoxItemInfoStockPercentage;
    private final MessageKey.Arg2<Long, Float> commandBoxItemInfoStockInServer;

    public LanguageProvider(DefaultMessageCollector collector) {
        this.modeDisplayName = MessageKey.key(collector.add("box.stats.mode.display-name", "Item Statistics"));
        this.guiLoading = MessageKey.key(collector.add("box.stats.gui.loading", "<gray>Loading..."));
        this.guiClickToRefresh = MessageKey.key(collector.add("box.stats.gui.click-to-refresh", "<gray>Click to refresh"));
        this.guiItemDisplayNameWithRank = MessageKey.arg2(collector.add("box.stats.gui.item.display-name", "<reset><item><reset> <aqua>#<rank>"), item -> Argument.component("item", item), RANK_PLACEHOLDER);
        this.guiItemStock = MessageKey.arg1(collector.add("box.stats.gui.item.stock", "<gray>Stock: <aqua><amount>"), AMOUNT_PLACEHOLDER);
        this.guiItemStockPercentage = MessageKey.arg1(collector.add("box.stats.gui.item.stock-percentage", "<dark_gray>  <percentage>% of all item stock"), PERCENTAGE_PLACEHOLDER);
        this.guiItemStockInServer = MessageKey.arg1(collector.add("box.stats.gui.item.stock-in-server", "<gray>Server: <aqua><amount>"), AMOUNT_PLACEHOLDER);
        this.guiItemStockPercentageInServer = MessageKey.arg1(collector.add("box.stats.gui.item.stock-percentage-in-server", "<dark_gray>  <percentage>% of all stock"), PERCENTAGE_PLACEHOLDER);
        this.guiGlobalDisplayName = MessageKey.key(collector.add("box.stats.gui.global.display-name", "<gold>Global Statistics"));
        this.guiGlobalStock = MessageKey.arg1(collector.add("box.stats.gui.global.stock", "<gray>Total Stock: <aqua><amount>"), AMOUNT_PLACEHOLDER);
        this.commandBoxItemInfoStockPercentage = MessageKey.arg2(collector.add("box.stats.command.box.iteminfo.stock-percentage", "<gray>Stock Statistics: <aqua><percentage>%<gray> of all item stock (<aqua>#<rank><gray>)"), PERCENTAGE_PLACEHOLDER, RANK_PLACEHOLDER);
        this.commandBoxItemInfoStockInServer = MessageKey.arg2(collector.add("box.stats.command.box.iteminfo.stock-in-server", "<gray>Server Stock: <aqua><amount><gray> (<aqua><percentage>%<gray> of all stock)"), AMOUNT_PLACEHOLDER, PERCENTAGE_PLACEHOLDER);
    }

    public ComponentLike modeDisplayName() {
        return this.modeDisplayName;
    }

    public ComponentLike guiLoading() {
        return this.guiLoading;
    }

    public ComponentLike guiClickToRefresh() {
        return this.guiClickToRefresh;
    }

    public MessageKey.Arg2<Component, Integer> guiItemDisplayNameWithRank() {
        return this.guiItemDisplayNameWithRank;
    }

    public MessageKey.Arg1<Long> guiItemStock() {
        return this.guiItemStock;
    }

    public MessageKey.Arg1<Float> guiItemStockPercentage() {
        return this.guiItemStockPercentage;
    }

    public MessageKey.Arg1<Long> guiItemStockInServer() {
        return this.guiItemStockInServer;
    }

    public MessageKey.Arg1<Float> guiItemStockPercentageInServer() {
        return this.guiItemStockPercentageInServer;
    }

    public MessageKey guiGlobalDisplayName() {
        return this.guiGlobalDisplayName;
    }

    public MessageKey.Arg1<Long> guiGlobalStock() {
        return this.guiGlobalStock;
    }

    public MessageKey.Arg2<Float, Integer> commandBoxItemInfoStockPercentage() {
        return this.commandBoxItemInfoStockPercentage;
    }

    public MessageKey.Arg2<Long, Float> commandBoxItemInfoStockInServer() {
        return this.commandBoxItemInfoStockInServer;
    }
}
