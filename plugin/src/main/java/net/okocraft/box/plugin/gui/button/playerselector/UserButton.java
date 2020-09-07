package net.okocraft.box.plugin.gui.button.playerselector;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.gui.button.AbstractButton;
import net.okocraft.box.plugin.model.User;

public class UserButton extends AbstractButton {
    
    private final User selection;
    private final Consumer<User> callback;

    public UserButton(User selection, Consumer<User> callback) {
        super(createHeadIcon(Bukkit.getOfflinePlayer(selection.getUuid())));
        this.selection = selection;
        this.callback = callback;
        
        icon.applyConfig("user-selection-element");
        //TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        callback.accept(selection);
    }

    @Override
    public void update() {
    }
}
