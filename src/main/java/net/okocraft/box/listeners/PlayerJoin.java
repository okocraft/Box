package net.okocraft.box.listeners;

import lombok.val;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import net.okocraft.box.ConfigManager;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

public class PlayerJoin implements Listener {
    private final Database database;
    private final ConfigManager config;

    public PlayerJoin(Database database, Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.database = database;
        this.config   = Box.getInstance().getConfigManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        val player = event.getPlayer();
        val uuid   = player.getUniqueId().toString();
        val name   = player.getName().toLowerCase();

        // 新規プレイヤーが昔いた他のプレイヤーと同じ名前でログインしたら昔のプレイヤーの名前を消す。
        val registeredUuid = database.get("uuid", name);
        if (!registeredUuid.equals(uuid)) {
            database.set("player", registeredUuid, "");
        }

        // 初めてログインするプレイヤーを登録する。
        if (!database.existPlayer(name)) {
            database.addPlayer(uuid, name, false);

            // Grasp default value.
            val autoStore = String.valueOf(config.isAutoStoreEnabledByDefault());

            config.getAllItems().forEach(itemName ->
                    database.set("autostore_" + itemName, uuid, autoStore)
            );
        }

        // プレイヤーが過去の名前とは違う名前だったらデータベースを更新する。
        val oldName = database.get("player", uuid);
        if (!oldName.equalsIgnoreCase(name)) {
            database.set("player", uuid, name);
        }
    }
}
