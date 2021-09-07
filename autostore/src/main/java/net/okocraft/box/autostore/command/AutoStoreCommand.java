package net.okocraft.box.autostore.command;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.autostore.message.AutoStoreMessage;
import net.okocraft.box.autostore.model.SettingManager;
import net.okocraft.box.autostore.model.mode.AllModeSetting;
import net.okocraft.box.autostore.model.mode.PerItemModeSetting;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoStoreCommand extends AbstractCommand {

    private final SettingManager settingManager;

    public AutoStoreCommand(@NotNull SettingManager settingManager) {
        super("autostore", "box.command.autostore", Set.of("a", "as"));
        this.settingManager = settingManager;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length < 2) {
            // TODO: GUI
            player.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            return;
        }

        if (args[1].isEmpty()) {
            player.sendMessage(AutoStoreMessage.COMMAND_MODE_NOT_SPECIFIED);
            return;
        }

        var setting = settingManager.get(player);

        boolean allMode; // true is AllMode, false is PerItemMode

        if (isAll(args[1])) {
            allMode = true;
        } else if (isPerItem(args[1])) {
            allMode = false;
        } else {
            player.sendMessage(AutoStoreMessage.COMMAND_MODE_NOT_FOUND.apply(args[1]));
            return;
        }

        if (args.length < 3) {
            boolean sendTip = false;

            if (allMode) {
                var current = setting.getCurrentMode();
                var allModeSetting = setting.getAllModeSetting();

                if (current == allModeSetting) {
                    player.sendMessage(AutoStoreMessage.COMMAND_ALL_MODE_TOGGLED.apply(allModeSetting.toggleEnabled()));
                    return;
                }

                setting.setMode(allModeSetting);
                sendTip = !allModeSetting.isEnabled();
            } else {
                setting.setMode(setting.getPerItemModeSetting());
            }

            player.sendMessage(AutoStoreMessage.COMMAND_MODE_CHANGED.apply(setting.getCurrentMode()));

            if (sendTip) {
                player.sendMessage(AutoStoreMessage.COMMAND_TIP_ALL_MODE_DISABLED);
                player.sendMessage(AutoStoreMessage.COMMAND_TIP_HOW_TO_TOGGLE_ALL_MODE);
            }

            return;
        }

        if (allMode) {
            processAllMode(player, args, setting.getAllModeSetting());
        } else {
            processPerItemMode(player, args, setting.getPerItemModeSetting());
        }
    }

    private void processAllMode(@NotNull Player player, @NotNull String[] args, @NotNull AllModeSetting setting) {
        if (args[2].isEmpty()) {
            return;
        }

        Boolean enabled = getBoolean(args[2]);

        if (enabled == null) {
            player.sendMessage(AutoStoreMessage.COMMAND_NOT_BOOLEAN.apply(args[2]));
            return;
        }

        setting.setEnabled(enabled);
        player.sendMessage(AutoStoreMessage.COMMAND_ALL_MODE_TOGGLED.apply(enabled));
    }

    private void processPerItemMode(@NotNull Player player, @NotNull String[] args, @NotNull PerItemModeSetting setting) {
        if (args[2].isEmpty()) {
            return;
        }

        var itemManager = BoxProvider.get().getItemManager();

        var optionalBoxItem = itemManager.getBoxItem(args[2]);

        if (optionalBoxItem.isEmpty()) {
            if (4 < args[2].length() || args.length < 4) {
                player.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[2]));
                return;
            }

            if (isAll(args[2])) {
                Boolean bool = getBoolean(args[3]);

                if (bool != null) {
                    setting.setEnabledItems(bool ? itemManager.getBoxItemSet() : Collections.emptyList());
                    player.sendMessage(AutoStoreMessage.COMMAND_PER_ITEM_ALL_TOGGLED.apply(bool));
                } else {
                    player.sendMessage(AutoStoreMessage.COMMAND_NOT_BOOLEAN.apply(args[2]));
                }
            } else {
                player.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[2]));
            }

            return;
        }

        var boxItem = optionalBoxItem.get();
        Boolean bool = 3 < args.length ? getBoolean(args[3]) : null;

        if (bool != null) {
            setting.setEnabled(boxItem, bool);
        } else {
            bool = setting.toggleEnabled(boxItem);
        }

        player.sendMessage(AutoStoreMessage.COMMAND_PER_ITEM_ITEM_TOGGLED.apply(boxItem, bool));
    }

    private @Nullable Boolean getBoolean(@NotNull String arg) {
        // for aliases: t = true, f = false, of = off
        if (arg.charAt(0) == 't' || arg.equalsIgnoreCase("on")) {
            return true;
        } else if (arg.charAt(0) == 'f' || arg.startsWith("of")) {
            return false;
        } else {
            return null;
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return Stream.of("all", "peritem")
                    .filter(mode -> mode.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && isAll(args[1])) {
            return Stream.of("true", "false")
                    .filter(bool -> bool.startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        if (isPerItem(args[1])) {
            if (args.length == 3) {
                var itemNameFilter = args[2].toUpperCase(Locale.ROOT);

                var result =
                        BoxProvider.get()
                                .getItemManager()
                                .getItemNameSet()
                                .stream()
                                .filter(itemName -> itemName.startsWith(itemNameFilter))
                                .sorted()
                                .collect(Collectors.toList());

                if ("all".startsWith(args[2].toLowerCase(Locale.ROOT))) {
                    result.add("all");
                }

                return result;
            }

            if (args.length == 4) {
                return Stream.of("true", "false")
                        .filter(bool -> bool.startsWith(args[3].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private boolean isAll(@NotNull String arg) {
        return !arg.isEmpty() && arg.length() < 4 && (arg.charAt(0) == 'a' || arg.charAt(0) == 'A');
    }

    private boolean isPerItem(@NotNull String arg) {
        return !arg.isEmpty() && arg.length() < 8 && (arg.charAt(0) == 'p' || arg.charAt(0) == 'P');
    }
}
