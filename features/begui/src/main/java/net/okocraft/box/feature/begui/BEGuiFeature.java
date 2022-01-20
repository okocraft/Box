package net.okocraft.box.feature.begui;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.begui.internal.mode.StorageDepositMode;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.feature.begui.internal.command.MenuOpenCommand;
import net.okocraft.box.feature.begui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.begui.internal.listener.InventoryListener;
import net.okocraft.box.feature.begui.internal.mode.StorageWithdrawMode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class BEGuiFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private final MenuOpenCommand command = new MenuOpenCommand();
    private Command commandOverriding = null;
    private final InventoryListener listener = new InventoryListener();

    private final StorageWithdrawMode storageWithdrawMode = new StorageWithdrawMode();
    private final StorageDepositMode storageDepositMode = new StorageDepositMode();

    public BEGuiFeature() {
        super("begui");

        for (BoxFeature feature : BoxProvider.get().getFeatures()) {
            if (feature instanceof GuiFeature guiFeature) {
                Command guiMenuOpenCommand = guiFeature.getMenuOpenCommand();
                this.commandOverriding = new Command() {
                    @Override
                    public @NotNull String getName() {
                        return guiMenuOpenCommand.getName();
                    }

                    @Override
                    public @NotNull String getPermissionNode() {
                        return guiMenuOpenCommand.getPermissionNode();
                    }

                    @Override
                    public @NotNull @Unmodifiable Set<String> getAliases() {
                        return guiMenuOpenCommand.getAliases();
                    }

                    @Override
                    public @NotNull Component getHelp() {
                        return guiMenuOpenCommand.getHelp();
                    }

                    @Override
                    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
                        if (sender instanceof Player player && player.getUniqueId().toString().startsWith("00000000")) {
                            command.onCommand(sender, args);
                        } else {
                            guiMenuOpenCommand.onCommand(sender, args);
                        }
                    }

                    @Override
                    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
                        if (sender instanceof Player player && player.getUniqueId().toString().startsWith("00000000")) {
                            return command.onTabComplete(sender, args);
                        } else {
                            return guiMenuOpenCommand.onTabComplete(sender, args);
                        }
                    }
                };
                break;
            }
        }
    }

    @Override
    public void enable() {
        var boxCommand = BoxProvider.get().getBoxCommand();

        ClickModeRegistry.register(storageWithdrawMode);
        ClickModeRegistry.register(storageDepositMode);

        if (commandOverriding != null) {
            boxCommand.changeNoArgumentCommand(commandOverriding);
        }
        boxCommand.getSubCommandHolder().register(command);

        Bukkit.getPluginManager().registerEvents(listener, BoxProvider.get().getPluginInstance());
    }

    @Override
    public void disable() {
        var boxCommand = BoxProvider.get().getBoxCommand();

        ClickModeRegistry.unregister(storageWithdrawMode);
        ClickModeRegistry.unregister(storageDepositMode);

        if (commandOverriding != null) {
            boxCommand.changeNoArgumentCommand(null);
        }
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
            sender.sendMessage(Component.translatable("box.begui.reloaded", NamedTextColor.GRAY));
        } catch (Exception ignored) {
            // I don't know why it loops infinitely and throws an exception when the message send to the console.
            // It's probably a bug of Paper or Adventure.
            //
            // IllegalStateException: Exceeded maximum depth of 512 while attempting to flatten components!
        }
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(CategoryFeature.class, GuiFeature.class);
    }
}
