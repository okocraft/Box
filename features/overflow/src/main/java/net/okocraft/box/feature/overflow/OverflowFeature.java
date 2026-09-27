package net.okocraft.box.feature.overflow;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.model.stock.StockEventCaller;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.holder.StorageHolder;
import org.jetbrains.annotations.NotNull;

public class OverflowFeature extends AbstractBoxFeature {

    private OverflowListener listener;

    public OverflowFeature(@NotNull FeatureContext.Registration ignored) {
        super("overflow");
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        if (!StorageHolder.isInitialized()) {
            BoxLogger.logger().warn("Overflow feature is disabled because storage is not loaded yet.");
            return;
        }

        BoxAPI api = BoxAPI.api();

        try {
            OverflowStockHolderMap holderMap = new OverflowStockHolderMap(api.getCustomDataManager());
            OverflowStockHolderStore holderStore = new OverflowStockHolderStore(
                StorageHolder.getStorage().getStockStorage(),
                api.getStockManager(),
                StockEventCaller.createDefault(api.getEventCallers().async())
            );

            this.listener = new OverflowListener(holderMap, holderStore);
            this.listener.register();
        } catch (Exception e) {
            BoxLogger.logger().error("Could not load overflow stock holder mappings.", e);
        }
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        if (this.listener != null) {
            this.listener.unregister();
            this.listener = null;
        }
    }
}
