package net.okocraft.box.listeners;

import net.okocraft.box.ConfigManager;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class PlayerJoin implements Listener {
    private final Database database;
    private final ConfigManager configManager;

    public PlayerJoin(Database database, Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.database = database;
        this.configManager = Box.getInstance().getConfigManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uniqueId = player.getUniqueId().toString();
        String currentName = player.getName().toLowerCase();

        // 新規プレイヤーが昔いた他のプレイヤーと同じ名前でログインしたら昔のプレイヤーの名前を消す。
        String registeredUniqueId = database.get("uuid", currentName);
        if (!registeredUniqueId.equals(uniqueId))
            database.set("player", registeredUniqueId, "");

        // 初めてログインするプレイヤーを登録する。
        if (!database.existPlayer(currentName)) {
            database.addPlayer(uniqueId, currentName, false);
            String autoStoreDefaultValue = String.valueOf(configManager.isAutoStoreEnabledByDefault());
            configManager.getAllItems().forEach(itemName -> database.set("autostore_" + itemName, uniqueId, autoStoreDefaultValue));
        }

        // プレイヤーが過去の名前とは違う名前だったらデータベースを更新する。
        String beforeName = database.get("player", uniqueId);
        if (!beforeName.equalsIgnoreCase(currentName))
            database.set("player", uniqueId, currentName);
    }
}
