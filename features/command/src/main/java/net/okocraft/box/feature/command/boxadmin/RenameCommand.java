package net.okocraft.box.feature.command.boxadmin;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RenameCommand extends AbstractCommand {

    public RenameCommand() {
        super("rename", "box.command.admin.rename");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            return;
        }

        var itemManager = BoxProvider.get().getItemManager();

        var item = itemManager.getBoxItem(args[1]);

        if (item.isEmpty()) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[1]));
            return;
        }

        var boxItem = item.get();

        if (!itemManager.isCustomItem(boxItem)) {
            sender.sendMessage(BoxAdminMessage.RENAME_IS_NOT_CUSTOM_ITEM.apply(boxItem));
            return;
        }

        var newName = args[2].toUpperCase(Locale.ROOT);

        if (itemManager.isUsed(newName)) {
            sender.sendMessage(BoxAdminMessage.RENAME_ALREADY_USED_NAME.apply(newName));
            return;
        }

        itemManager.renameCustomItem((BoxCustomItem) boxItem, newName)
                .thenAcceptAsync(customItem -> sender.sendMessage(BoxAdminMessage.RENAME_SUCCESS.apply(customItem)))
                .exceptionallyAsync(throwable -> {
                    sender.sendMessage(BoxAdminMessage.RENAME_FAILURE.apply(throwable));
                    return null;
                });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            var itemManager = BoxProvider.get().getItemManager();

            return itemManager.getBoxItemSet()
                    .stream()
                    .filter(itemManager::isCustomItem)
                    .map(BoxItem::getPlainName)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}