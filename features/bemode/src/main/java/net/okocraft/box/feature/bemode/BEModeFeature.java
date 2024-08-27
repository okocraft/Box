package net.okocraft.box.feature.bemode;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.feature.bemode.listener.ClickModeCheckListener;
import net.okocraft.box.feature.bemode.mode.StorageDepositMode;
import net.okocraft.box.feature.bemode.mode.StorageWithdrawMode;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class BEModeFeature extends AbstractBoxFeature {

    private final StorageDepositMode storageDepositMode;
    private final StorageWithdrawMode storageWithdrawMode;

    public BEModeFeature(@NotNull FeatureContext.Registration context) {
        super("bemode");
        var currentStock = Arg1.arg1(context.defaultMessageCollector().add("box.bemode.current-stock", "<gray>Current stock: <aqua><current>"), Placeholders.CURRENT);
        this.storageDepositMode = new StorageDepositMode(currentStock, context.defaultMessageCollector());
        this.storageWithdrawMode = new StorageWithdrawMode(currentStock, context.defaultMessageCollector());
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        ClickModeRegistry.register(this.storageDepositMode);
        ClickModeRegistry.register(this.storageWithdrawMode);
        ClickModeCheckListener.register();
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        ClickModeRegistry.unregister(this.storageDepositMode);
        ClickModeRegistry.unregister(this.storageWithdrawMode);
        ClickModeCheckListener.unregister();
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(GuiFeature.class);
    }
}
