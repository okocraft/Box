package net.okocraft.box.feature.gui;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.gui.internal.command.MenuOpenCommand;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import net.okocraft.box.feature.gui.internal.listener.InventoryListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class GuiFeature extends AbstractBoxFeature implements Reloadable {

    private final MenuOpenCommand command;
    private final InventoryListener listener = new InventoryListener();

    public GuiFeature(@NotNull FeatureContext.Registration context) {
        super("gui");
        this.command = new MenuOpenCommand(context.defaultMessageCollector());
        DisplayKeys.addDefaults(context.defaultMessageCollector());
        BoxInventoryHolder.addDefaultErrorMessage(context.defaultMessageCollector());
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        var boxCommand = BoxAPI.api().getBoxCommand();

        boxCommand.changeNoArgumentCommand(command);
        boxCommand.getSubCommandHolder().register(command);

        Bukkit.getPluginManager().registerEvents(this.listener, context.plugin());
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        var boxCommand = BoxAPI.api().getBoxCommand();

        boxCommand.changeNoArgumentCommand(null);
        boxCommand.getSubCommandHolder().unregister(command);

        this.closeMenus();
        HandlerList.unregisterAll(listener);
    }

    @Override
    public void reload(@NotNull FeatureContext.Reloading context) {
        this.closeMenus();
    }

    private void closeMenus() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (BoxInventoryHolder.isBoxMenu(player.getOpenInventory().getTopInventory())) {
                player.closeInventory();
            }
        });
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(CategoryFeature.class);
    }
}
