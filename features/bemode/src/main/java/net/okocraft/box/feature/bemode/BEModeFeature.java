package net.okocraft.box.feature.bemode;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
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

    private static final Key MENU_OPEN_EVENT_LISTENER_KEY = Key.key("box", "feature/bemode/listener");

    private final StorageDepositMode storageDepositMode = new StorageDepositMode();
    private final StorageWithdrawMode storageWithdrawMode = new StorageWithdrawMode();

    public BEModeFeature() {
        super("bemode");
    }

    @Override
    public void enable() {
        ClickModeRegistry.register(storageDepositMode);
        ClickModeRegistry.register(storageWithdrawMode);
        BoxAPI.api().getEventManager().getSubscriber(MenuOpenEvent.class).subscribe(MENU_OPEN_EVENT_LISTENER_KEY, new MenuOpenListener());
    }

    @Override
    public void disable() {
        ClickModeRegistry.unregister(storageDepositMode);
        ClickModeRegistry.unregister(storageWithdrawMode);
        BoxAPI.api().getEventManager().getSubscriber(MenuOpenEvent.class).unsubscribeByKey(MENU_OPEN_EVENT_LISTENER_KEY);
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(GuiFeature.class);
    }
}
