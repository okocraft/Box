package net.okocraft.box.feature.notifier;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.feature.notifier.listener.StockHolderListener;

public class NotifierFeature extends AbstractBoxFeature implements Disableable {

    private static final Key STOCK_EVENT_LISTENER_KEY = Key.key("box", "feature/notifier/stock_event_listener");

    private final StockHolderListener stockHolderListener = new StockHolderListener();

    public NotifierFeature() {
        super("notifier");
    }

    @Override
    public void enable() {
        this.stockHolderListener.register(STOCK_EVENT_LISTENER_KEY);
    }

    @Override
    public void disable() {
        this.stockHolderListener.unregister(STOCK_EVENT_LISTENER_KEY);
    }
}
