
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

public final class Config extends CustomConfig {

    private final Random random = new Random();

    public Config() {
        super("config.yml");
    }

    public List<String> getDisabledWorlds() {
        return get().getStringList("disabled-worlds");
    }

    public List<String> getAutoReplantWorlds() {
        return get().getStringList("auto-replant-worlds");
    }

    public int getMaxQuantity() {
        return get().getInt("gui.max-quantity", 640);
    }

    public boolean isAutoStoreEnabled() {
        return get().getBoolean("auto-store.enabled", true);
    }

    public boolean getDefaultAutoStoreValue() {
        return get().getBoolean("auto-store.default", false);
    }

    public void playGUIOpenSound(Player player) {
        playSound(player, "open", Sound.BLOCK_CHEST_OPEN);
    }
    
    public void playDepositSound(Player player) {
        playSound(player, "deposit", Sound.ENTITY_ITEM_PICKUP);
    }
    
    public void playWithdrawSound(Player player) {
        playSound(player, "withdraw", Sound.BLOCK_STONE_BUTTON_CLICK_ON);
    }
    
    public void playBuySound(Player player) {
        playSound(player, "buy", Sound.ENTITY_ITEM_PICKUP);
    }
    
    public void playSellSound(Player player) {
        playSound(player, "sell", Sound.BLOCK_STONE_BUTTON_CLICK_ON);
    }
    
    public void playCraftSound(Player player) {
        playSound(player, "craft", Sound.BLOCK_ANVIL_PLACE);
    }
    
    public void playNotEnoughSound(Player player) {
        playSound(player, "not-enough", Sound.ENTITY_ENDERMAN_TELEPORT);
    }
    
    public void playDecreaseUnitSound(Player player) {
        playSound(player, "decrease-unit", Sound.BLOCK_TRIPWIRE_CLICK_OFF);
    }
    
    public void playIncreaseUnitSound(Player player) {
        playSound(player, "increase-unit", Sound.BLOCK_TRIPWIRE_CLICK_ON);
    }
    
    public void playChangePageSound(Player player) {
        playSound(player, "change-page", Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }
    
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

    @Override
    public void reload() {
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory);
        super.reload();
    }
}