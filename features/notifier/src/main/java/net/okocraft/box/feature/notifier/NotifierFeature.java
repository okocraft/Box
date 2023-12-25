package net.okocraft.box.feature.notifier;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.feature.notifier.listener.StockHolderListener;
import org.jetbrains.annotations.NotNull;

public class NotifierFeature extends AbstractBoxFeature implements Disableable {

    private static final Key STOCK_EVENT_LISTENER_KEY = Key.key("box", "feature/notifier/stock_event_listener");

    private final StockHolderListener stockHolderListener = new StockHolderListener();

    public NotifierFeature(@NotNull FeatureContext.Registration ignored) {
        super("notifier");
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        this.stockHolderListener.register(STOCK_EVENT_LISTENER_KEY);
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        this.stockHolderListener.unregister(STOCK_EVENT_LISTENER_KEY);
    }
}
