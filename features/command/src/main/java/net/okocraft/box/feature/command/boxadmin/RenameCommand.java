package net.okocraft.box.feature.command.boxadmin;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.result.item.ItemRenameResult;
import net.okocraft.box.api.util.BoxLogger;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.okocraft.box.api.message.Placeholders.ERROR;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.ITEM_NAME;

public class RenameCommand extends AbstractCommand {

    private final MessageKey.Arg1<String> success;
    private final MessageKey.Arg1<BoxItem> notCustomItem;
    private final MessageKey.Arg1<String> usedName;
    private final MessageKey.Arg1<Throwable> exceptionOccurred;
    private final MessageKey help;

    public RenameCommand(@NotNull DefaultMessageCollector collector) {
        super("rename", "box.admin.command.rename");
        this.success = MessageKey.arg1(collector.add("box.command.boxadmin.rename.success", "<gray>The item has been renamed to <aqua><item_name><gray>."), ITEM_NAME);
        this.notCustomItem = MessageKey.arg1(collector.add("box.command.boxadmin.rename.not-custom-item", "<red>The item <aqua><item><red> cannot be renamed."), ITEM);
        this.usedName = MessageKey.arg1(collector.add("box.command.boxadmin.rename.used-name", "<aqua><item_name><red> is already used."), ITEM_NAME);
        this.exceptionOccurred = MessageKey.arg1(collector.add("box.command.boxadmin.rename.exception-occurred", "<red>Failed to rename the item. Error message: <white><error>"), ERROR);
        this.help = MessageKey.key(collector.add("box.command.boxadmin.rename.help", "<aqua>/boxadmin rename <current name> <new name><dark_gray> - <gray>Changes item name"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        var itemManager = BoxAPI.api().getItemManager();

        var item = itemManager.getBoxItem(args[1]);

        if (item.isEmpty()) {
            sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(args[1]));
            return;
        }

        var boxItem = item.get();

        if (itemManager.isCustomItem(boxItem)) {
            itemManager.renameCustomItem((BoxCustomItem) boxItem, args[2], result -> this.consumeResult(sender, result));
        } else {
            sender.sendMessage(this.notCustomItem.apply(boxItem));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            var itemManager = BoxAPI.api().getItemManager();

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
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }

    private void consumeResult(@NotNull CommandSender sender, @NotNull ItemRenameResult result) {
        switch (result) {
            case ItemRenameResult.Success successResult ->
                sender.sendMessage(this.success.apply(successResult.customItem().getPlainName()));
            case ItemRenameResult.DuplicateName duplicateNameResult ->
                sender.sendMessage(this.usedName.apply(duplicateNameResult.name()));
            case ItemRenameResult.ExceptionOccurred exceptionOccurredResult -> {
                Exception ex = exceptionOccurredResult.exception();
                sender.sendMessage(this.exceptionOccurred.apply(ex));
                BoxLogger.logger().error("Could not rename a custom item.", ex);
            }
        }
    }
}
