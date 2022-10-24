package net.okocraft.box.feature.autostore.command;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AutoStoreItemCommand extends AutoStoreSubCommand {

    public AutoStoreItemCommand() {
        super("item");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull AutoStoreSetting setting) {
        // set all mode false
        if (args.length < 3) {
            sender.sendMessage(AutoStoreMessage.COMMAND_MODE_CHANGED.apply(false));

            if (AutoStoreCommandUtil.enableAutoStore(setting, sender) || setting.isAllMode()) {
                setting.setAllMode(false);
                AutoStoreCommandUtil.callEvent(setting);
            }
            return;
        }

        var perItemModeSetting = setting.getPerItemModeSetting();
        var itemManager = BoxProvider.get().getItemManager();
        var optionalBoxItem = itemManager.getBoxItem(args[2]);

        if (optionalBoxItem.isEmpty()) {
            if (args.length < 4 || !isAll(args[2])) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[2]));
                return;
            }

            Boolean bool = AutoStoreCommandUtil.getBoolean(args[3]);
            if (bool == null) {
                sender.sendMessage(AutoStoreMessage.COMMAND_NOT_BOOLEAN.apply(args[3]));
                return;
            }

            AutoStoreCommandUtil.enableAutoStore(setting, sender);
            changeToPerItemMode(setting, sender);

            perItemModeSetting.setEnabledItems(bool ? itemManager.getBoxItemSet() : Collections.emptyList());
            sender.sendMessage(AutoStoreMessage.COMMAND_PER_ITEM_ALL_TOGGLED.apply(bool));

            AutoStoreCommandUtil.callEvent(setting);

            return;
        }

        var boxItem = optionalBoxItem.get();
        Boolean bool = 3 < args.length ? AutoStoreCommandUtil.getBoolean(args[3]) : null;

        AutoStoreCommandUtil.enableAutoStore(setting, sender);
        changeToPerItemMode(setting, sender);

        if (bool != null) {
            perItemModeSetting.setEnabled(boxItem, bool);
        } else {
            bool = perItemModeSetting.toggleEnabled(boxItem);
        }

        sender.sendMessage(AutoStoreMessage.COMMAND_PER_ITEM_ITEM_TOGGLED.apply(boxItem, bool));
        AutoStoreCommandUtil.callEvent(setting);
    }

    private boolean isAll(@NotNull String arg) {
        return !arg.isEmpty() && arg.length() < 4 && (arg.charAt(0) == 'a' || arg.charAt(0) == 'A');
    }

    private void changeToPerItemMode(@NotNull AutoStoreSetting setting, @NotNull CommandSender sender) {
        if (setting.isAllMode()) {
            setting.setAllMode(false);
            sender.sendMessage(AutoStoreMessage.COMMAND_MODE_CHANGED.apply(setting.isAllMode()));
        }
    }

    @Override
    public @NotNull List<String> runTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 3) {
            var result = TabCompleter.itemNames(args[2].toUpperCase(Locale.ROOT));

            if ("all".startsWith(args[2].toLowerCase(Locale.ROOT))) {
                result.add("all");
            }

            return result;
        }

        if (args.length == 4) {
            return Stream.of("on", "off")
                    .filter(bool -> bool.startsWith(args[3].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
