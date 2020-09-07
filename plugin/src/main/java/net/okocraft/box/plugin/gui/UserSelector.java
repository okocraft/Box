package net.okocraft.box.plugin.gui;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.gui.button.playerselector.UserButton;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.manager.UserManager;

public class UserSelector extends BoxInventoryHolder {

    public UserSelector(@NotNull String title, Consumer<User> callback) {
        super(MenuType.PLAYER_SELECTOR, title);

        UserManager um = JavaPlugin.getPlugin(Box.class).getUserManager();
        putElementAndPageArrow(Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId).map(um::getUser)
                .map(user -> new UserButton(user, callback))
                .collect(Collectors.toList())
        );
        setItems();
    }
}
