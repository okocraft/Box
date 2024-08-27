package net.okocraft.box.feature.stick.command;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.stick.item.BoxStickItem;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class StickCommand extends AbstractCommand {

    private static final String DEFAULT_DISPLAY_NAME = "<blue>Box Stick";
    private static final String DEFAULT_LORE = """
        <gray>Holding this in offhand to
        <gray>consume items from Box when use them.
        <gray>Also, press RMB to open the menu.""";

    private final BoxStickItem boxStickItem;

    private final MiniMessageBase defaultDisplayName;
    private final MiniMessageBase defaultLore;

    private final MiniMessageBase success;
    private final MiniMessageBase alreadyInOffhand;
    private final MiniMessageBase fullInventory;
    private final MiniMessageBase help;

    public StickCommand(@NotNull BoxStickItem boxStickItem, @NotNull DefaultMessageCollector collector) {
        super("stick", "box.command.stick", Set.of("s"));
        this.boxStickItem = boxStickItem;

        this.defaultDisplayName = messageKey(collector.add("box.stick.default-stick.display-name", DEFAULT_DISPLAY_NAME));
        this.defaultLore = messageKey(collector.add("box.stick.default-stick.lore", DEFAULT_LORE));

        this.success = messageKey(collector.add("box.stick.command.stick.success", "<gray>Set your offhand to Box Stick."));
        this.alreadyInOffhand = messageKey(collector.add("box.stick.command.stick.already-in-offhand", "<red>You already have Box Stick in your offhand."));
        this.fullInventory = messageKey(collector.add("box.stick.command.stick.full-inventory", "<red>Could not give your offhand Box Stick because your inventory is full."));
        this.help = messageKey(collector.add("box.stick.command.stick.help", "<aqua>/box stick<dark_gray> - <gray>Set your offhand to Box Stick"));
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

        var inventory = player.getInventory();
        var currentOffHand = inventory.getItemInOffHand();

        if (!currentOffHand.getType().isAir()) {
            if (this.boxStickItem.check(currentOffHand)) {
                this.alreadyInOffhand.source(msgSrc).send(player);
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
                this.fullInventory.source(msgSrc).send(player);
                return;
            }

            storage[firstEmpty] = currentOffHand;
            inventory.setStorageContents(storage);
        }

        inventory.setItemInOffHand(this.createDefaultStick(player));
        this.success.source(msgSrc).send(player);
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }

    private @NotNull ItemStack createDefaultStick(@NotNull Player player) {
        var src = BoxAPI.api().getMessageProvider().findSource(player);
        return ItemEditor.create()
            .displayName(this.defaultDisplayName.create(src))
            .loreEmptyLine()
            .loreLines(this.defaultLore.create(src))
            .loreEmptyLine()
            .editMeta(meta -> this.boxStickItem.saveBoxStickKey(meta.getPersistentDataContainer()))
            .createItem(Material.STICK);
    }
}
