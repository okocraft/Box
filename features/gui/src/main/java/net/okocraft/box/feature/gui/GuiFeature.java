package net.okocraft.box.feature.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.feature.gui.internal.command.MenuOpenCommand;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.listener.InventoryListener;
import net.okocraft.box.feature.gui.internal.mode.StorageDepositMode;
import net.okocraft.box.feature.gui.internal.mode.StorageMode;
import net.okocraft.box.feature.gui.internal.mode.StorageWithdrawMode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class GuiFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private final MenuOpenCommand command = new MenuOpenCommand();
    private final InventoryListener listener = new InventoryListener();

    private final StorageMode storageMode = new StorageMode();

    private final StorageDepositMode storageDepositMode = new StorageDepositMode();
    private final StorageWithdrawMode storageWithdrawMode = new StorageWithdrawMode();

    public GuiFeature() {
        super("gui");
    }

    @Override
    public void enable() {
        var boxCommand = BoxProvider.get().getBoxCommand();

        ClickModeRegistry.register(storageMode);
        ClickModeRegistry.register(storageDepositMode);
        ClickModeRegistry.register(storageWithdrawMode);

        boxCommand.changeNoArgumentCommand(command);
        boxCommand.getSubCommandHolder().register(command);

        Bukkit.getPluginManager().registerEvents(listener, BoxProvider.get().getPluginInstance());
    }

    @Override
    public void disable() {
        var boxCommand = BoxProvider.get().getBoxCommand();

        ClickModeRegistry.unregister(storageMode);
        ClickModeRegistry.unregister(storageDepositMode);
        ClickModeRegistry.unregister(storageWithdrawMode);

        boxCommand.changeNoArgumentCommand(null);
        boxCommand.getSubCommandHolder().unregister(command);

        if (Bukkit.isPrimaryThread()) {
            closeMenus();
        } else {
            BoxProvider.get().getTaskFactory().run(this::closeMenus).join();
        }

        HandlerList.unregisterAll(listener);
    }

    private void closeMenus() {
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> p.getOpenInventory().getTopInventory().getHolder() instanceof BoxInventoryHolder)
                .forEach(HumanEntity::closeInventory);
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();

        try {
            sender.sendMessage(Component.translatable("box.gui.reloaded", NamedTextColor.GRAY));
        } catch (Exception ignored) {
            // I don't know why it loops infinitely and throws an exception when the message send to the console.
            // It's probably a bug of Paper or Adventure.
            //
            // IllegalStateException: Exceeded maximum depth of 512 while attempting to flatten components!
        }
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(CategoryFeature.class);
    }
}
