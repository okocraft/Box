package net.okocraft.box.feature.stick.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.okocraft.box.api.message.Components.commandHelp;
import static net.okocraft.box.api.message.Components.grayTranslatable;
import static net.okocraft.box.api.message.Components.redTranslatable;

public class CustomStickCommand extends AbstractCommand {

    private static final Component CUSTOM_STICK_HELP = commandHelp("box.stick.command.customstick");
    private static final Component CUSTOM_STICK_SUCCESS = grayTranslatable("box.stick.command.customstick.success");
    private static final Component CUSTOM_STICK_ALREADY = redTranslatable("box.stick.command.customstick.already");
    private static final Component CUSTOM_STICK_IS_AIR = redTranslatable("box.stick.command.customstick.is-air");

    private final BoxStickItem boxStickItem;

    public CustomStickCommand(@NotNull BoxStickItem boxStickItem) {
        super("customstick", "box.admin.command.customstick");
        this.boxStickItem = boxStickItem;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        BoxProvider.get().getTaskFactory().runEntityTask(player, this::makeStick).join();
    }

    private void makeStick(@NotNull Player player) {
        var item = player.getInventory().getItemInMainHand();
        var meta = item.getItemMeta();

        if (meta == null) {
            player.sendMessage(CUSTOM_STICK_IS_AIR);
            return;
        }

        if (boxStickItem.check(item)) {
            player.sendMessage(CUSTOM_STICK_ALREADY);
            return;
        }

        boxStickItem.saveBoxStickKey(meta.getPersistentDataContainer());

        item.setItemMeta(meta);

        player.sendMessage(CUSTOM_STICK_SUCCESS);
    }

    @Override
    public @NotNull Component getHelp() {
        return CUSTOM_STICK_HELP;
    }
}
