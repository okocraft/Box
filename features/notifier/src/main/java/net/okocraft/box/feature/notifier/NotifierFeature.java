package net.okocraft.box.feature.notifier;

import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.feature.notifier.listener.StockHolderListener;

public class NotifierFeature extends AbstractBoxFeature implements Disableable {

    private final StockHolderListener stockHolderListener = new StockHolderListener();

    public NotifierFeature() {
        super("notifier");
    }

    @Override
    public void enable() {
        stockHolderListener.register(getListenerKey());
    }

    @Override
    public void disable() {
        stockHolderListener.unregister();
    }
}
