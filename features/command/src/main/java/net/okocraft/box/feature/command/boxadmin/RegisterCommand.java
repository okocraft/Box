package net.okocraft.box.feature.command.boxadmin;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.arg.Arg2;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.result.item.ItemRegistrationResult;
import net.okocraft.box.api.util.BoxLogger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.arg.Arg2.arg2;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.ERROR;
import static net.okocraft.box.api.message.Placeholders.ITEM_NAME;

public class RegisterCommand extends AbstractCommand {

    private static final Placeholder<ItemStack> ITEM_STACK = Placeholder.component("item_stack", item -> item.displayName().hoverEvent(item));

    private final Arg2<ItemStack, String> success;
    private final MiniMessageBase renameTip;
    private final MiniMessageBase isAir;
    private final Arg1<String> usedName;
    private final Arg1<ItemStack> alreadyRegistered;
    private final Arg1<Throwable> exceptionOccurred;
    private final MiniMessageBase help;

    public RegisterCommand(@NotNull DefaultMessageCollector collector) {
        super("register", "box.admin.command.register");
        this.success = arg2(collector.add("box.command.boxadmin.register.success", "<aqua><item_stack><gray> has been registered as <aqua><item_name><gray>."), ITEM_STACK, ITEM_NAME);
        this.renameTip = messageKey(collector.add("box.command.boxadmin.register.rename-tip", "<gray>Registered items can be renamed with <aqua>/boxadmin rename<gray>."));
        this.isAir = messageKey(collector.add("box.command.boxadmin.register.is-air", "<red>You have no item in your main hand."));
        this.usedName = arg1(collector.add("box.command.boxadmin.register.used-name", "<aqua><item_name><red> is already used."), ITEM_NAME);
        this.alreadyRegistered = arg1(collector.add("box.command.boxadmin.register.already-registered", "<aqua><item_stack><red> is already registered."), ITEM_STACK);
        this.exceptionOccurred = arg1(collector.add("box.command.boxadmin.register.exception-occurred", "<red>Failed to register the item. Error message: <white><error>"), ERROR);
        this.help = MiniMessageBase.messageKey(collector.add("box.command.boxadmin.register.help", "<aqua>/boxadmin register<dark_gray> - <gray>Registers item in main hand to Box"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (!(sender instanceof Player player)) {
            ErrorMessages.COMMAND_ONLY_PLAYER.source(msgSrc).send(sender);
            return;
        }

        BoxAPI.api().getScheduler().runEntityTask(player, () -> {
            var mainHandItem = player.getInventory().getItemInMainHand();

            if (mainHandItem.getType().isAir()) {
                this.isAir.source(msgSrc).send(sender);
            } else {
                BoxAPI.api().getItemManager().registerCustomItem(
                        mainHandItem.clone(),
                        1 < args.length ? args[1] : null,
                        result -> this.consumeResult(player, result)
                );
            }
        });
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }

    private void consumeResult(@NotNull Player player, @NotNull ItemRegistrationResult result) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(player);
        switch (result) {
            case ItemRegistrationResult.Success successResult -> {
                this.success.apply(successResult.customItem().getOriginal(), successResult.customItem().getPlainName()).source(msgSrc).send(player);
                this.renameTip.source(msgSrc).send(player);
            }
            case ItemRegistrationResult.DuplicateName duplicateNameResult ->
                    this.usedName.apply(duplicateNameResult.name()).source(msgSrc).send(player);
            case ItemRegistrationResult.DuplicateItem duplicateItemResult ->
                    this.alreadyRegistered.apply(duplicateItemResult.item()).source(msgSrc).send(player);
            case ItemRegistrationResult.ExceptionOccurred exceptionOccurredResult -> {
                var ex = exceptionOccurredResult.exception();
                this.exceptionOccurred.apply(ex).source(msgSrc).send(player);
                BoxLogger.logger().error("Could not register a new custom item.", ex);
            }
        }
    }
}
