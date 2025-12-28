package net.okocraft.box.feature.stick.command;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomStickCommand extends AbstractCommand {

    private final MessageKey success;
    private final MessageKey isStick;
    private final MessageKey isAir;
    private final MessageKey help;

    private final BoxStickItem boxStickItem;

    public CustomStickCommand(@NotNull BoxStickItem boxStickItem, @NotNull DefaultMessageCollector collector) {
        super("customstick", "box.admin.command.customstick");
        this.boxStickItem = boxStickItem;

        this.success = MessageKey.key(collector.add("box.stick.command.customstick.success", "<gray>The item in your hand can now be used as Box Stick."));
        this.isStick = MessageKey.key(collector.add("box.stick.command.customstick.is-stick", "<red>The item in your hand can already be used as Box Stick."));
        this.isAir = MessageKey.key(collector.add("box.stick.command.customstick.is-air", "<red>You have no item in your main hand."));
        this.help = MessageKey.key(collector.add("box.stick.command.customstick.help", "<aqua>/boxadmin customstick<dark_gray> - <gray>Makes item in main hand a Box Stick"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            BoxAPI.api().getScheduler().runEntityTask(player, () -> this.runCommand(player));
        } else {
            sender.sendMessage(ErrorMessages.COMMAND_ONLY_PLAYER);
        }
    }

    private void runCommand(@NotNull Player player) {
        var item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(this.isAir);
            return;
        }

        if (this.boxStickItem.check(item)) {
            player.sendMessage(this.isStick);
            return;
        }

        item.editPersistentDataContainer(this.boxStickItem::saveBoxStickKey);
        player.sendMessage(this.success);
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
