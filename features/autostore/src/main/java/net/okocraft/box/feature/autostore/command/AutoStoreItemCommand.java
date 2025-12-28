package net.okocraft.box.feature.autostore.command;

import dev.siroshun.mcmsgdef.MessageKey;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AutoStoreItemCommand extends AutoStoreSubCommand {

    private final MessageKey perItemModeEnabled;
    private final MessageKey allEnabled;
    private final MessageKey allDisabled;
    private final MessageKey.Arg1<BoxItem> itemEnabled;
    private final MessageKey.Arg1<BoxItem> itemDisabled;

    AutoStoreItemCommand(@NotNull DefaultMessageCollector collector) {
        super("item");
        this.perItemModeEnabled = MessageKey.key(collector.add("box.autostore.command.per-item.mode-changed", "<gray>Auto-store mode is now <aqua>per-item<gray>."));
        this.allEnabled = MessageKey.key(collector.add("box.autostore.command.per-item.all-enabled", "<gray>Auto-store settings for all items have been <green>enabled<gray>."));
        this.allDisabled = MessageKey.key(collector.add("box.autostore.command.per-item.all-disabled", "<gray>Auto-store settings for all items have been <red>disabled<gray>."));
        this.itemEnabled = MessageKey.arg1(collector.add("box.autostore.command.per-item.item-enabled", "<gray>Auto-store setting of the item <aqua><item><gray> has been <green>enabled<gray>."), Placeholders.ITEM);
        this.itemDisabled = MessageKey.arg1(collector.add("box.autostore.command.per-item.item-disabled", "<gray>Auto-store setting of the item <aqua><item><gray> has been <red>disabled<gray>."), Placeholders.ITEM);
    }

    @Override
    void runCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull AutoStoreSetting setting) {
        // set all mode false
        if (args.length < 3) {
            sender.sendMessage(this.perItemModeEnabled);

            if (AutoStoreCommandUtil.changeAutoStore(setting, sender, true, false) || setting.isAllMode()) {
                setting.setAllMode(false);
                AutoStoreCommandUtil.callEvent(setting);
            }
            return;
        }

        var perItemModeSetting = setting.getPerItemModeSetting();
        var itemManager = BoxAPI.api().getItemManager();
        var optionalBoxItem = itemManager.getBoxItem(args[2]);

        if (optionalBoxItem.isEmpty()) {
            if (args.length < 4 || !this.isAll(args[2])) {
                sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(args[2]));
                return;
            }

            Boolean result = AutoStoreCommandUtil.getBoolean(args[3]);
            if (result == null) {
                sender.sendMessage(AutoStoreCommandUtil.NOT_BOOLEAN.apply(args[3]));
                return;
            }

            AutoStoreCommandUtil.changeAutoStore(setting, sender, true, false);
            this.changeToPerItemMode(setting, sender);

            perItemModeSetting.clearAndEnableItems(result ? itemManager.getItemIdList() : IntSet.of());
            sender.sendMessage(result ? this.allEnabled : this.allDisabled);

            AutoStoreCommandUtil.callEvent(setting);

            return;
        }

        var boxItem = optionalBoxItem.get();
        boolean result;

        if (3 < args.length) {
            var temp = AutoStoreCommandUtil.getBoolean(args[3]);

            if (temp == null) {
                sender.sendMessage(AutoStoreCommandUtil.NOT_BOOLEAN.apply(args[3]));
                return;
            }

            perItemModeSetting.setEnabled(boxItem, temp);
            result = temp;
        } else {
            result = perItemModeSetting.toggleEnabled(boxItem);
        }

        AutoStoreCommandUtil.changeAutoStore(setting, sender, true, false);
        this.changeToPerItemMode(setting, sender);

        sender.sendMessage((result ? this.itemEnabled : this.itemDisabled).apply(boxItem));
        AutoStoreCommandUtil.callEvent(setting);
    }

    private boolean isAll(@NotNull String arg) {
        return !arg.isEmpty() && arg.length() < 4 && (arg.charAt(0) == 'a' || arg.charAt(0) == 'A');
    }

    private void changeToPerItemMode(@NotNull AutoStoreSetting setting, @NotNull CommandSender sender) {
        if (setting.isAllMode()) {
            sender.sendMessage(this.perItemModeEnabled);
        }
    }

    @Override
    @NotNull List<String> runTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 3) {
            var result = TabCompleter.itemNames(args[2]);

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
