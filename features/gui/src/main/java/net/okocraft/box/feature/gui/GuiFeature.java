package net.okocraft.box.feature.gui;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.feature.gui.internal.command.MenuOpenCommand;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.hook.autostore.AutoStoreHook;
import net.okocraft.box.feature.gui.internal.listener.InventoryListener;
import net.okocraft.box.feature.gui.internal.mode.StorageMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;

public class GuiFeature extends AbstractBoxFeature {

    private final MenuOpenCommand command = new MenuOpenCommand();
    private final InventoryListener listener = new InventoryListener();

    private final StorageMode storageMode = new StorageMode();

    public GuiFeature() {
        super("gui");
    }

    @Override
    public void enable() {
        var boxCommand = BoxProvider.get().getBoxCommand();

        ClickModeRegistry.register(storageMode);

        AutoStoreHook.enableIfRegistered();
        AutoStoreHook.registerListener();

        boxCommand.changeNoArgumentCommand(command);
        boxCommand.getSubCommandHolder().register(command);

        Bukkit.getPluginManager().registerEvents(listener, BoxProvider.get().getPluginInstance());
    }

    @Override
    public void disable() {
        var boxCommand = BoxProvider.get().getBoxCommand();

        ClickModeRegistry.unregister(storageMode);

        AutoStoreHook.disable();
        AutoStoreHook.unregisterListener();

        boxCommand.changeNoArgumentCommand(null);
        boxCommand.getSubCommandHolder().unregister(command);

        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.getOpenInventory().getTopInventory().getHolder() instanceof BoxInventoryHolder)
                .forEach(HumanEntity::closeInventory);

        HandlerList.unregisterAll(listener);
    }
}
