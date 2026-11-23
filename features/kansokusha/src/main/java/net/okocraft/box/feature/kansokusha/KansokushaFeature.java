package net.okocraft.box.feature.kansokusha;

import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.stockholder.StockHolderResetEvent;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public final class KansokushaFeature extends AbstractBoxFeature {

    private static final Key LISTENER_KEY = Key.key("box", "feature/kansokusha/listener");
    private final SubscribedListenerHolder listeners = new SubscribedListenerHolder();

    public KansokushaFeature(FeatureContext.Registration ignored) {
        super("kansokusha");
    }

    @Override
    public void enable(FeatureContext.Enabling context) {
        if (!context.plugin().getServer().getPluginManager().isPluginEnabled("Kansokusha")) {
            return;
        }

        try {
            var recorder = StockChangeRecorder.connect(BoxAPI.api().getItemManager());
            this.listeners.subscribeAll(subscriber -> subscriber
                .add(StockEvent.class, LISTENER_KEY, recorder::onStockChange, Priority.NORMAL)
                .add(StockHolderResetEvent.class, LISTENER_KEY, recorder::onReset, Priority.NORMAL));
        } catch (RuntimeException | LinkageError e) {
            this.unsubscribe();
            BoxLogger.logger().warn("Could not enable Kansokusha stock logging.", e);
        }
    }

    @Override
    public void disable(FeatureContext.Disabling context) {
        this.unsubscribe();
    }

    private void unsubscribe() {
        if (!this.listeners.getListeners().isEmpty()) {
            this.listeners.unsubscribeAll();
        }
    }
}
