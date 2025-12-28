package net.okocraft.box.feature.command.boxadmin;

import dev.siroshun.mcmsgdef.MessageKey;
import dev.siroshun.mcmsgdef.Placeholder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.translation.Argument;
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

import static net.okocraft.box.api.message.Placeholders.ERROR;
import static net.okocraft.box.api.message.Placeholders.ITEM_NAME;

public class RegisterCommand extends AbstractCommand {

    private static final Placeholder<ItemStack> ITEM_STACK = item -> Argument.component("item_stack", item.displayName().hoverEvent(item));

    private final MessageKey.Arg2<ItemStack, String> success;
    private final MessageKey renameTip;
    private final MessageKey isAir;
    private final MessageKey.Arg1<String> usedName;
    private final MessageKey.Arg1<ItemStack> alreadyRegistered;
    private final MessageKey.Arg1<Throwable> exceptionOccurred;
    private final MessageKey help;

    public RegisterCommand(@NotNull DefaultMessageCollector collector) {
        super("register", "box.admin.command.register");
        this.success = MessageKey.arg2(collector.add("box.command.boxadmin.register.success", "<aqua><item_stack><gray> has been registered as <aqua><item_name><gray>."), ITEM_STACK, ITEM_NAME);
        this.renameTip = MessageKey.key(collector.add("box.command.boxadmin.register.rename-tip", "<gray>Registered items can be renamed with <aqua>/boxadmin rename<gray>."));
        this.isAir = MessageKey.key(collector.add("box.command.boxadmin.register.is-air", "<red>You have no item in your main hand."));
        this.usedName = MessageKey.arg1(collector.add("box.command.boxadmin.register.used-name", "<aqua><item_name><red> is already used."), ITEM_NAME);
        this.alreadyRegistered = MessageKey.arg1(collector.add("box.command.boxadmin.register.already-registered", "<aqua><item_stack><red> is already registered."), ITEM_STACK);
        this.exceptionOccurred = MessageKey.arg1(collector.add("box.command.boxadmin.register.exception-occurred", "<red>Failed to register the item. Error message: <white><error>"), ERROR);
        this.help = MessageKey.key(collector.add("box.command.boxadmin.register.help", "<aqua>/boxadmin register<dark_gray> - <gray>Registers item in main hand to Box"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ErrorMessages.COMMAND_ONLY_PLAYER);
            return;
        }

        BoxAPI.api().getScheduler().runEntityTask(player, () -> {
            var mainHandItem = player.getInventory().getItemInMainHand();

            if (mainHandItem.getType().isAir()) {
                sender.sendMessage(this.isAir);
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
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }

    private void consumeResult(@NotNull Player player, @NotNull ItemRegistrationResult result) {
        switch (result) {
            case ItemRegistrationResult.Success successResult -> {
                player.sendMessage(this.success.apply(successResult.customItem().getOriginal(), successResult.customItem().getPlainName()));
                player.sendMessage(this.renameTip);
            }
            case ItemRegistrationResult.DuplicateName duplicateNameResult ->
                player.sendMessage(this.usedName.apply(duplicateNameResult.name()));
            case ItemRegistrationResult.DuplicateItem duplicateItemResult ->
                player.sendMessage(this.alreadyRegistered.apply(duplicateItemResult.item()));
            case ItemRegistrationResult.ExceptionOccurred exceptionOccurredResult -> {
                var ex = exceptionOccurredResult.exception();
                player.sendMessage(this.exceptionOccurred.apply(ex));
                BoxLogger.logger().error("Could not register a new custom item.", ex);
            }
        }
    }
}
