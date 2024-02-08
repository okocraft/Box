package net.okocraft.box.feature.notifier;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.feature.notifier.listener.StockHolderListener;
import org.jetbrains.annotations.NotNull;

public class NotifierFeature extends AbstractBoxFeature {

    public NotifierFeature(@NotNull FeatureContext.Registration ignored) {
        super("notifier");
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        StockHolderListener.register(BoxAPI.api().getEventManager());
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        StockHolderListener.unregister(BoxAPI.api().getEventManager());
    }
}
