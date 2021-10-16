package net.okocraft.box.feature.stick.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CustomStickCommand extends AbstractCommand {

    private static final Component CUSTOM_STICK_HELP =
            translatable("box.stick.command.customstick.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.stick.command.customstick.help.description", GRAY));

    private static final Component CUSTOM_STICK_SUCCESS =
            translatable()
                    .key("box.stick.command.customstick.success")
                    .color(GRAY)
                    .build();

    private static final Component CUSTOM_STICK_ALREADY =
            translatable()
                    .key("box.stick.command.customstick.already")
                    .color(RED)
                    .build();

    private static final Component CUSTOM_STICK_IS_AIR =
            translatable()
                    .key("box.stick.command.customstick.is-air")
                    .color(RED)
                    .build();

    private final NamespacedKey key;

    public CustomStickCommand(@NotNull NamespacedKey key) {
        super("customstick", "box.admin.command.customstick");
        this.key = key;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        BoxProvider.get().getTaskFactory().run(() -> makeStick(player)).join();
    }

    private void makeStick(@NotNull Player player) {
        var item = player.getInventory().getItemInMainHand();
        var meta = item.getItemMeta();

        if (meta == null) {
            player.sendMessage(CUSTOM_STICK_IS_AIR);
            return;
        }

        var dataContainer = meta.getPersistentDataContainer();

        if (dataContainer.has(key, PersistentDataType.BYTE)) {
            player.sendMessage(CUSTOM_STICK_ALREADY);
            return;
        }

        dataContainer.set(key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);

        player.sendMessage(CUSTOM_STICK_SUCCESS);
    }

    @Override
    public @NotNull Component getHelp() {
        return CUSTOM_STICK_HELP;
    }
}
