
package net.okocraft.box.config;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.okocraft.box.Box;

/**
 * データベース設定や、全体的に作用する設定を取得できる。
 */
public final class Config extends CustomConfig {

    private final Random random = new Random();

    /**
     * コンストラクタ。
     * @deprecated 内部利用限定。このコンストラクタを使わず、{@code Box.getInstance().getAPI().getConfig()}を使用すること。
     */
    @Deprecated
    public Config() {
        super("config.yml");
    }

    /**
     * MySQLを使うかどうかを取得する。
     * 
     * @return MySQLを使う場合はtrue、さもなくばfalse
     */
    public boolean usingMySQL() {
        return get().getString("database.type", "sqlite").equalsIgnoreCase("mysql");
    }

    /**
     * MySQLを使う場合のホストを取得する。
     * 
     * @return host。デフォルトで"localhost"を返す
     */
    public String getMySQLHost() {
        return get().getString("database.mysql-settings.host", "localhost");
    }

    /**
     * MySQLを使う場合のポートを取得する。
     * 
     * @return port。デフォルトで3306を返す
     */
    public int getMySQLPort() {
        return get().getInt("database.mysql-settings.port", 3306);
    }

    /**
     * MySQLを使う場合のユーザーネームを取得する。
     * 
     * @return user。デフォルトで"root"を返す
     */
    public String getMySQLUser() {
        return get().getString("database.mysql-settings.user", "root");
    }

    /**
     * MySQLを使う場合のパスワードを取得する。
     * 
     * @return password。デフォルトで"pass"を返す
     */
    public String getMySQLPass() {
        return get().getString("database.mysql-settings.password", "pass");
    }

    /**
     * MySQLを使う場合のデータベース名を取得する。
     * 
     * @return database name。デフォルトで"box_database"を返す
     */
    public String getMySQLDatabaseName() {
        return get().getString("database.mysql-settings.db-name", "box_database");
    }

    /**
     * Boxプラグインが無効化されているワールドのリストを取得する。
     * 
     * @return プラグインが無効化されているワールドのリスト
     */
    public List<String> getDisabledWorlds() {
        return get().getStringList("disabled-worlds");
    }

    /**
     * 自動で苗の植え替えを行うワールドのリストを取得する。
     * ここで指定されたワールドでは、麦やジャガイモなどを収穫すると、Boxから自動的に種を消費するようになる。
     * 
     * @return 自動植え替えが有効化されるワールドのリスト
     */
    public List<String> getAutoReplantWorlds() {
        // TODO: box stickに移動。
        return get().getStringList("auto-replant-worlds");
    }

    /**
     * GUIで行う引き出し・預け入れ・購入・売却・クラフトにおける、一度の取引数の最大値を取得する。
     * 
     * @return 一度の取引数の最大値
     */
    public int getMaxQuantity() {
        return get().getInt("gui.max-quantity", 640);
    }

    /**
     * サーバー全体でautostoreが有効化されているかどうかを取得する。
     * 
     * @return autostoreが有効化されていればtrue、さもなくばfalse
     */
    public boolean isAutoStoreEnabled() {
        return get().getBoolean("autostore", true);
    }

    /**
     * GUIが開かれたとき、カテゴリが選択されたときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playGUIOpenSound(Player player) {
        playSound(player, "open", Sound.BLOCK_CHEST_OPEN);
    }
    
    /**
     * GUIでアイテムを預けた時の音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playDepositSound(Player player) {
        playSound(player, "deposit", Sound.ENTITY_ITEM_PICKUP);
    }
    
    /**
     * GUIでアイテムを引き出したときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playWithdrawSound(Player player) {
        playSound(player, "withdraw", Sound.BLOCK_STONE_BUTTON_CLICK_ON);
    }
    
    /**
     * GUIでアイテムを購入したときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playBuySound(Player player) {
        playSound(player, "buy", Sound.ENTITY_ITEM_PICKUP);
    }
    
    /**
     * GUIでアイテムを売却したときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playSellSound(Player player) {
        playSound(player, "sell", Sound.BLOCK_STONE_BUTTON_CLICK_ON);
    }
    
    /**
     * GUIでアイテムをクラフトしたときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playCraftSound(Player player) {
        playSound(player, "craft", Sound.BLOCK_ANVIL_PLACE);
    }
    
    /**
     * GUIでアイテムを引き出そうとしたものの、ストックが足りずに引き出せなかったときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playNotEnoughSound(Player player) {
        playSound(player, "not-enough", Sound.ENTITY_ENDERMAN_TELEPORT);
    }
    
    /**
     * GUIで一度の取引数を減らしたときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     * 
     * @see Config#getMaxQuantity()
     */
    public void playDecreaseUnitSound(Player player) {
        playSound(player, "decrease-unit", Sound.BLOCK_TRIPWIRE_CLICK_OFF);
    }
    
    /**
     * GUIで一度の取引数を増やしたときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     * 
     * @see Config#getMaxQuantity()
     */
    public void playIncreaseUnitSound(Player player) {
        playSound(player, "increase-unit", Sound.BLOCK_TRIPWIRE_CLICK_ON);
    }
    
    /**
     * GUIでページを変更したときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playChangePageSound(Player player) {
        playSound(player, "change-page", Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }
    
    /**
     * GUIで前のメニューに戻ったときの音を{@code player}に再生する。
     * 
     * @param player 音を再生するプレイヤー
     */
    public void playBackMenuSound(Player player) {
        playSound(player, "deposit", Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }

    private void playSound(Player player, String soundKey, Sound def) {
        String key = "sound-settings." + soundKey + ".";
        Sound sound;
        try {
            sound = Sound.valueOf(get().getString(key + "sound").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sound = def;
        }

        float pitch = getRandomValue(key, true);
        float volume = getRandomValue(key, false);

        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }

    private float getRandomValue(String key, boolean isPitch) {
        List<Double> pitchList = get().getDoubleList(key + (isPitch ? "pitch" : "volume"));
        if (pitchList.size() == 2) {
            float min = (float) Objects.requireNonNullElse(pitchList.get(0), 0D).doubleValue();
            float max = (float) Objects.requireNonNullElse(pitchList.get(1), 2D).doubleValue();
            return Math.max(Math.min((max - min) * ((float) random.nextDouble()) + min, 2.0F), 0F);
        } else {
            return 1F;
        }
    }

    /**
     * Box stickとなるアイテムスタックを取得する。
     * 
     * @return box stick
     */
    public ItemStack getStick() {
        String displayName = ChatColor.translateAlternateColorCodes('&',
                get().getString("box-stick.display-name", "&9Box Stick"));
        List<String> lore = get().getStringList("box-stick.lore");
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Box.getInstance(), "boxstick"), PersistentDataType.INTEGER, 1);
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;   
    }

    /**
     * 
     */
    @Override
    public void reload() {
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory);
        super.reload();
    }
}