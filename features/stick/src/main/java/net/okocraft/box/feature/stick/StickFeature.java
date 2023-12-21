package net.okocraft.box.feature.stick;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.gui.api.event.MenuOpenEvent;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.menu.CategorySelectorMenu;
import net.okocraft.box.feature.stick.command.CustomStickCommand;
import net.okocraft.box.feature.stick.command.StickCommand;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import net.okocraft.box.feature.stick.listener.StickListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StickFeature extends AbstractBoxFeature implements Disableable {

    private final BoxStickItem boxStickItem = new BoxStickItem(new NamespacedKey("box", "stick"));
    private final StickCommand stickCommand = new StickCommand(boxStickItem);
    private final CustomStickCommand customStickCommand = new CustomStickCommand(boxStickItem);
    private final StickListener stickListener = new StickListener(boxStickItem);

    public StickFeature() {
        super("stick");
    }

    @Override
    public void enable() {
        if (BoxAPI.api().getFeature(GuiFeature.class).isPresent()) {
            this.boxStickItem.onRightClick(this::defaultRightClickAction);
        }

        BoxAPI.api().getBoxCommand().getSubCommandHolder().register(stickCommand);
        BoxAPI.api().getBoxAdminCommand().getSubCommandHolder().register(customStickCommand);
        Bukkit.getPluginManager().registerEvents(stickListener, BoxAPI.api().getPluginInstance());
    }

    @Override
    public void disable() {
        BoxAPI.api().getBoxCommand().getSubCommandHolder().unregister(stickCommand);
        BoxAPI.api().getBoxAdminCommand().getSubCommandHolder().register(customStickCommand);
        HandlerList.unregisterAll(stickListener);
    }

    /**
     * Gets the {@link BoxStickItem}.
     *
     * @return the {@link BoxStickItem}
     */
    public @NotNull BoxStickItem getBoxStickItem() {
        return boxStickItem;
    }

    private void defaultRightClickAction(@NotNull Player player) {
        if (player.hasPermission("box.stick.menu")) {
            var menu = new CategorySelectorMenu();
            var session = PlayerSession.newSession(player);

            BoxAPI.api().getEventManager().callAsync(new MenuOpenEvent(menu, session), event -> {
                if (!event.isCancelled()) {
                    MenuOpener.open(menu, session);
                }
            });
        }
    }
}
