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
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class StickCommand extends AbstractCommand {

    private static final Component COULD_NOT_GIVE_STICK =
            translatable("box.stick.command.could-not-give-stick", RED);
    private static final Component GIVE_SUCCESS =
            translatable("box.stick.command.success", GRAY);

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

        CompletableFuture.runAsync(
                () -> runCommand(player),
                BoxProvider.get().getExecutorProvider().getMainThread()
        ).join();
    }

    private void runCommand(@NotNull Player player) {
        var inventory = player.getInventory();
        var currentOffHand = inventory.getItemInOffHand();

        if (!currentOffHand.getType().isAir()) {
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
}
