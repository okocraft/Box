package net.okocraft.box.feature.stick.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.okocraft.box.api.message.Components.commandHelp;
import static net.okocraft.box.api.message.Components.grayTranslatable;
import static net.okocraft.box.api.message.Components.redTranslatable;

public class StickCommand extends AbstractCommand {

    private static final Component ALREADY_HAVE = redTranslatable("box.stick.command.already-have");

    private static final Component COULD_NOT_GIVE_STICK = redTranslatable("box.stick.command.could-not-give-stick");

    private static final Component GIVE_SUCCESS = grayTranslatable("box.stick.command.success");

    private static final Component HELP = commandHelp("box.stick.command");

    private final BoxStickItem boxStickItem;

    public StickCommand(@NotNull BoxStickItem boxStickItem) {
        super("stick", "box.command.stick", Set.of("s"));
        this.boxStickItem = boxStickItem;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        BoxProvider.get().getScheduler().runEntityTask(player, () -> this.runCommand(player));
    }

    private void runCommand(@NotNull Player player) {
        var inventory = player.getInventory();
        var currentOffHand = inventory.getItemInOffHand();

        if (!currentOffHand.getType().isAir()) {
            if (boxStickItem.check(currentOffHand)) {
                player.sendMessage(ALREADY_HAVE);
                return;
            }

            int firstEmpty = -1;
            var storage = inventory.getStorageContents();

            for (int slot = 0; slot < storage.length; slot++) {
                var item = storage[slot];

                if (item == null || item.getType().isAir()) {
                    firstEmpty = slot;
                }
            }

            if (firstEmpty == -1) {
                player.sendMessage(COULD_NOT_GIVE_STICK);
                return;
            }

            storage[firstEmpty] = currentOffHand;
            inventory.setStorageContents(storage);
        }

        inventory.setItemInOffHand(boxStickItem.create(player.locale()));
        player.sendMessage(GIVE_SUCCESS);
    }

    @Override
    public @NotNull Component getHelp() {
        return HELP;
    }
}
