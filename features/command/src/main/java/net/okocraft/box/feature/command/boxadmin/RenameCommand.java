package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.result.item.ItemRegistrationResult;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RenameCommand extends AbstractCommand {

    public RenameCommand() {
        super("rename", "box.admin.command.rename");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(getHelp());
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

        itemManager.renameCustomItem((BoxCustomItem) boxItem, args[2], result -> consumeResult(sender, result));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            var itemManager = BoxProvider.get().getItemManager();

            return itemManager.getItemList()
                    .stream()
                    .filter(itemManager::isCustomItem)
                    .map(BoxItem::getPlainName)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.RENAME_HELP;
    }

    private void consumeResult(@NotNull CommandSender sender, @NotNull ItemRegistrationResult result) {
        if (result instanceof ItemRegistrationResult.Success success) {
            sender.sendMessage(BoxAdminMessage.RENAME_SUCCESS.apply(success.customItem()));
        } else if (result instanceof ItemRegistrationResult.DuplicateName duplicateName) {
            sender.sendMessage(BoxAdminMessage.RENAME_ALREADY_USED_NAME.apply(duplicateName.name()));
        } else if (result instanceof ItemRegistrationResult.ExceptionOccurred exceptionOccurred) {
            var ex = exceptionOccurred.exception();
            sender.sendMessage(BoxAdminMessage.RENAME_FAILURE.apply(ex));
            BoxLogger.logger().error("Could not rename a custom item.", ex);
        }
    }
}
