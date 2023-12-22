package net.okocraft.box.feature.gui;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.util.Folia;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.gui.internal.command.MenuOpenCommand;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import net.okocraft.box.feature.gui.internal.listener.InventoryListener;
import net.okocraft.box.feature.gui.internal.mode.StorageMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GuiFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private final MenuOpenCommand command;
    private final InventoryListener listener = new InventoryListener();

    public GuiFeature(@NotNull FeatureContext.Registration context) {
        super("gui");
        DisplayKeys.addDefaults(context.defaultMessageCollector());
        new StorageMode(context.defaultMessageCollector()); // TODO
        this.command = new MenuOpenCommand(context.defaultMessageCollector());
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

        HandlerList.unregisterAll(listener);
    }

    @Override
    public void reload(@NotNull FeatureContext.Reloading context) {
        List<Player> schedulingPlayers = null;

        if (Folia.check()) {
            var players = Bukkit.getOnlinePlayers().toArray(Player[]::new);

            for (var player : players) {
                if (Bukkit.isOwnedByCurrentRegion(player)) {
                    closeMenu(player);
                } else {
                    if (schedulingPlayers == null) {
                        schedulingPlayers = new ArrayList<>(players.length);
                    }

                    schedulingPlayers.add(player);
                }
            }
        } else {
            if (Bukkit.isPrimaryThread()) {
                Bukkit.getOnlinePlayers().forEach(this::closeMenu);
            } else {
                schedulingPlayers = ObjectList.of(Bukkit.getOnlinePlayers().toArray(Player[]::new));
            }
        }

        if (schedulingPlayers != null && !schedulingPlayers.isEmpty()) {
            for (var player : schedulingPlayers) {
                BoxAPI.api().getScheduler().runEntityTask(player, () -> closeMenu(player));
            }
        }
    }

    private void closeMenu(@NotNull Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof BoxInventoryHolder) {
            player.closeInventory();
        }
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(CategoryFeature.class);
    }
}
