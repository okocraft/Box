package net.okocraft.box.feature.stick.command;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
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
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class StickCommand extends AbstractCommand {

    private static final String DEFAULT_DISPLAY_NAME = "<blue>Box Stick";
    private static final String DEFAULT_LORE = """
        <gray>Holding this in offhand to
        <gray>consume items from Box when use them.
        <gray>Also, press RMB to open the menu.""";

    private final BoxStickItem boxStickItem;

    private final MessageKey defaultDisplayName;
    private final MessageKey defaultLore;

    private final MessageKey success;
    private final MessageKey alreadyInOffhand;
    private final MessageKey fullInventory;
    private final MessageKey help;

    public StickCommand(@NotNull BoxStickItem boxStickItem, @NotNull DefaultMessageCollector collector) {
        super("stick", "box.command.stick", Set.of("s"));
        this.boxStickItem = boxStickItem;

        this.defaultDisplayName = MessageKey.key(collector.add("box.stick.default-stick.display-name", DEFAULT_DISPLAY_NAME));
        this.defaultLore = MessageKey.key(collector.add("box.stick.default-stick.lore", DEFAULT_LORE));

        this.success = MessageKey.key(collector.add("box.stick.command.stick.success", "<gray>Set your offhand to Box Stick."));
        this.alreadyInOffhand = MessageKey.key(collector.add("box.stick.command.stick.already-in-offhand", "<red>You already have Box Stick in your offhand."));
        this.fullInventory = MessageKey.key(collector.add("box.stick.command.stick.full-inventory", "<red>Could not give your offhand Box Stick because your inventory is full."));
        this.help = MessageKey.key(collector.add("box.stick.command.stick.help", "<aqua>/box stick<dark_gray> - <gray>Set your offhand to Box Stick"));
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
        PlayerInventory inventory = player.getInventory();
        ItemStack currentOffHand = inventory.getItemInOffHand();

        if (!currentOffHand.getType().isAir()) {
            if (this.boxStickItem.check(currentOffHand)) {
                player.sendMessage(this.alreadyInOffhand);
                return;
            }

            int firstEmpty = -1;
            ItemStack[] storage = inventory.getStorageContents();

            for (int slot = 0; slot < storage.length; slot++) {
                ItemStack item = storage[slot];

                if (item == null || item.getType().isAir()) {
                    firstEmpty = slot;
                }
            }

            if (firstEmpty == -1) {
                player.sendMessage(this.fullInventory);
                return;
            }

            storage[firstEmpty] = currentOffHand;
            inventory.setStorageContents(storage);
        }

        inventory.setItemInOffHand(this.createDefaultStick(player));
        player.sendMessage(this.success);
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }

    private @NotNull ItemStack createDefaultStick(Player player) {
        return ItemEditor.create()
            .displayName(this.defaultDisplayName)
            .loreEmptyLine()
            .loreLines(this.defaultLore)
            .loreEmptyLine()
            .editPersistentDataContainer(this.boxStickItem::saveBoxStickKey)
            .createItem(player, Material.STICK);
    }
}
