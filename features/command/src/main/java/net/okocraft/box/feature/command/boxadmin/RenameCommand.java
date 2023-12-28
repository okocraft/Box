package net.okocraft.box.feature.command.boxadmin;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
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

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.ERROR;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.ITEM_NAME;

public class RenameCommand extends AbstractCommand {

    private final Arg1<String> success;
    private final Arg1<BoxItem> notCustomItem;
    private final Arg1<String> usedName;
    private final Arg1<Throwable> exceptionOccurred;
    private final MiniMessageBase help;

    public RenameCommand(@NotNull DefaultMessageCollector collector) {
        super("rename", "box.admin.command.rename");
        this.success = arg1(collector.add("box.command.boxadmin.rename.success", "<gray>The item has been renamed to <aqua><item_name><gray>."), ITEM_NAME);
        this.notCustomItem = arg1(collector.add("box.command.boxadmin.rename.not-custom-item", "<red>The item <aqua><item><red> cannot be renamed."), ITEM);
        this.usedName = arg1(collector.add("box.command.boxadmin.rename.used-name", "<aqua><item_name><red> is already used."), ITEM_NAME);
        this.exceptionOccurred = arg1(collector.add("box.command.boxadmin.rename.exception-occurred", "<red>Failed to rename the item. Error message: <white><error>"), ERROR);
        this.help = messageKey(collector.add("box.command.boxadmin.rename.help", "<aqua>/boxadmin rename <current name> <new name><dark_gray> - <gray>Changes item name"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (args.length < 3) {
            ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
            sender.sendMessage(this.getHelp(msgSrc));
            return;
        }

        var itemManager = BoxAPI.api().getItemManager();

        var item = itemManager.getBoxItem(args[1]);

        if (item.isEmpty()) {
            ErrorMessages.ITEM_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
            return;
        }

        var boxItem = item.get();

        if (itemManager.isCustomItem(boxItem)) {
            itemManager.renameCustomItem((BoxCustomItem) boxItem, args[2], result -> this.consumeResult(sender, result));
        } else {
            this.notCustomItem.apply(boxItem).source(msgSrc).send(sender);
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
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }

    private void consumeResult(@NotNull CommandSender sender, @NotNull ItemRenameResult result) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);
        if (result instanceof ItemRenameResult.Success successResult) {
            this.success.apply(successResult.customItem().getPlainName()).source(msgSrc).send(sender);
        } else if (result instanceof ItemRenameResult.DuplicateName duplicateNameResult) {
            this.usedName.apply(duplicateNameResult.name()).source(msgSrc).send(sender);
        } else if (result instanceof ItemRenameResult.ExceptionOccurred exceptionOccurredResult) {
            var ex = exceptionOccurredResult.exception();
            this.exceptionOccurred.apply(ex).source(msgSrc).send(sender);
            BoxLogger.logger().error("Could not rename a custom item.", ex);
        }
    }
}
