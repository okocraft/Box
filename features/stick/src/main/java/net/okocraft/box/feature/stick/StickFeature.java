package net.okocraft.box.feature.stick;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link net.okocraft.box.api.feature.BoxFeature} that provides Box Stick.
 */
public class StickFeature extends AbstractBoxFeature {

    private final BoxStickItem boxStickItem;
    private final StickCommand stickCommand;
    private final CustomStickCommand customStickCommand;
    private StickListener stickListener;

    /**
     * The constructor of {@link StickFeature}.
     *
     * @param context a {@link net.okocraft.box.api.feature.FeatureContext.Registration} context
     */
    @ApiStatus.Internal
    public StickFeature(@NotNull FeatureContext.Registration context) {
        super("stick");
        this.boxStickItem = new BoxStickItem(new NamespacedKey("box", "stick"));
        this.stickCommand = new StickCommand(this.boxStickItem, context.defaultMessageCollector());
        this.customStickCommand = new CustomStickCommand(this.boxStickItem, context.defaultMessageCollector());
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        if (BoxAPI.api().getFeatureProvider().getFeature(GuiFeature.class).isPresent()) {
            this.boxStickItem.onRightClick(this::defaultRightClickAction);
        }

        BoxAPI.api().getBoxCommand().getSubCommandHolder().register(this.stickCommand);
        BoxAPI.api().getBoxAdminCommand().getSubCommandHolder().register(this.customStickCommand);

        this.stickListener = new StickListener(this.boxStickItem);
        Bukkit.getPluginManager().registerEvents(this.stickListener, context.plugin());
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        BoxAPI.api().getBoxCommand().getSubCommandHolder().unregister(this.stickCommand);
        BoxAPI.api().getBoxAdminCommand().getSubCommandHolder().register(this.customStickCommand);

        if (this.stickListener != null) {
            HandlerList.unregisterAll(this.stickListener);
        }
    }

    /**
     * Gets the {@link BoxStickItem}.
     *
     * @return the {@link BoxStickItem}
     */
    public @NotNull BoxStickItem getBoxStickItem() {
        return this.boxStickItem;
    }

    private void defaultRightClickAction(@NotNull Player player) {
        if (player.hasPermission("box.stick.menu") && BoxAPI.api().getBoxPlayerMap().isLoaded(player)) {
            var menu = new CategorySelectorMenu();
            var session = PlayerSession.newSession(player);

            BoxAPI.api().getEventCallers().async().call(new MenuOpenEvent(menu, session), event -> {
                if (!event.isCancelled()) {
                    MenuOpener.open(menu, session);
                }
            });
        }
    }
}
