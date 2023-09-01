package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.result.item.ItemRegistrationResult;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class RegisterCommand extends AbstractCommand {

    public RegisterCommand() {
        super("register", "box.admin.command.register");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        var mainHandItem = player.getInventory().getItemInMainHand();

        if (mainHandItem.getType().isAir()) {
            player.sendMessage(BoxAdminMessage.REGISTER_IS_AIR);
            return;
        }

        BoxProvider.get().getItemManager().registerCustomItem(
                mainHandItem,
                1 < args.length ? args[1] : null,
                result -> consumeResult(player, result)
        );
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.REGISTER_HELP;
    }

    private void consumeResult(@NotNull Player player, @NotNull ItemRegistrationResult result) {
        if (result instanceof ItemRegistrationResult.Success success) {
            player.sendMessage(BoxAdminMessage.REGISTER_SUCCESS.apply(success.customItem()));
            player.sendMessage(BoxAdminMessage.REGISTER_TIP_RENAME);
        } else if (result instanceof ItemRegistrationResult.DuplicateName duplicateName) {
            player.sendMessage(BoxAdminMessage.RENAME_ALREADY_USED_NAME.apply(duplicateName.name()));
        } else if (result instanceof ItemRegistrationResult.DuplicateItem duplicateItem) {
            player.sendMessage(BoxAdminMessage.REGISTER_ALREADY_REGISTERED.apply(duplicateItem.item()));
        } else if (result instanceof ItemRegistrationResult.ExceptionOccurred exceptionOccurred) {
            var ex = exceptionOccurred.exception();
            player.sendMessage(BoxAdminMessage.REGISTER_FAILURE.apply(ex));
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not register a new custom item.", ex);
        }
    }
}
