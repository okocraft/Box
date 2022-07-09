package net.okocraft.box.feature.bemode;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.feature.bemode.listener.MenuOpenListener;
import net.okocraft.box.feature.bemode.mode.StorageDepositMode;
import net.okocraft.box.feature.bemode.mode.StorageWithdrawMode;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.gui.api.event.MenuOpenEvent;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class BEModeFeature extends AbstractBoxFeature {

    private final StorageDepositMode storageDepositMode = new StorageDepositMode();
    private final StorageWithdrawMode storageWithdrawMode = new StorageWithdrawMode();

    public BEModeFeature() {
        super("bemode");
    }

    @Override
    public void enable() {
        ClickModeRegistry.register(storageDepositMode);
        ClickModeRegistry.register(storageWithdrawMode);

        BoxProvider.get().getEventBus()
                .getHandlerList(MenuOpenEvent.class)
                .subscribe(getListenerKey(), new MenuOpenListener());
    }

    @Override
    public void disable() {
        ClickModeRegistry.unregister(storageDepositMode);
        ClickModeRegistry.unregister(storageWithdrawMode);

        BoxProvider.get().getEventBus().unsubscribeAll(getListenerKey());
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(GuiFeature.class);
    }
}
