package net.okocraft.box.feature.stick.command;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class CustomStickCommand extends AbstractCommand {

    private final MiniMessageBase success;
    private final MiniMessageBase isStick;
    private final MiniMessageBase isAir;
    private final MiniMessageBase help;

    private final BoxStickItem boxStickItem;

    public CustomStickCommand(@NotNull BoxStickItem boxStickItem, @NotNull DefaultMessageCollector collector) {
        super("customstick", "box.admin.command.customstick");
        this.boxStickItem = boxStickItem;

        this.success = messageKey(collector.add("box.stick.command.customstick.success", "<gray>The item in your hand can now be used as Box Stick."));
        this.isStick = messageKey(collector.add("box.stick.command.customstick.is-stick", "<red>The item in your hand can already be used as Box Stick."));
        this.isAir = messageKey(collector.add("box.stick.command.customstick.is-air", "<red>You have no item in your main hand."));
        this.help = messageKey(collector.add("box.stick.command.customstick.help", "<aqua>/boxadmin customstick<dark_gray> - <gray>Makes item in main hand a Box Stick"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player player) {
            BoxAPI.api().getScheduler().runEntityTask(player, () -> this.runCommand(player));
        } else {
            ErrorMessages.COMMAND_ONLY_PLAYER.source(BoxAPI.api().getMessageProvider().findSource(sender)).send(sender);
        }
    }

    private void runCommand(@NotNull Player player) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(player);

        var item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            this.isAir.source(msgSrc).send(player);
            return;
        }

        if (this.boxStickItem.check(item)) {
            this.isStick.source(msgSrc).send(player);
            return;
        }

        item.editPersistentDataContainer(this.boxStickItem::saveBoxStickKey);
        this.success.source(msgSrc).send(player);
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
