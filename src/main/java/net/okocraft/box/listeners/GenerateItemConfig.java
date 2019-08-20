/*
 * Box
 * Copyright (C) 2019 OKOCRAFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.okocraft.box.listeners;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.okocraft.box.Box;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.ItemLanguage;

public class GenerateItemConfig implements Listener {

    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();

    private final Player player;
    private final String category;
    private final String id;
    private final String displayName;
    private final Material icon;

    public GenerateItemConfig(Player player, String category, String id, String displayName, String icon) {
        Bukkit.getPluginManager().registerEvents(this, INSTANCE);
        this.player = player;
        this.category = category;
        this.id = id;
        this.displayName = displayName;
        this.icon = Optional.ofNullable(Material.getMaterial(icon)).orElse(Material.AIR);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChestClick(PlayerInteractEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        Block chest = event.getClickedBlock();

        if (chest == null || event.getClickedBlock().getType() != Material.CHEST) {
            event.setCancelled(true);
            player.sendMessage("ブロックがチェストではなかったため、キャンセルされました。");
            HandlerList.unregisterAll(this);
            return;
        }

        HandlerList.unregisterAll(this);
        event.setCancelled(true);

        Path file = INSTANCE.getDataFolder().toPath().resolve(category + ".yml");
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException ex) {
                ex.printStackTrace();
                player.sendMessage("ファイルを作ることができなかったため、キャンセルされました。");
                return;
            }
        }

        Chest chestData = (Chest) chest.getState();
        Inventory chestSnapInv = chestData.getSnapshotInventory();

        FileConfiguration itemConfig = CONFIG.getItemConfig();
        String categoryPath = "categories." + category;
        itemConfig.set(categoryPath + ".id", id);
        itemConfig.set(categoryPath + ".display_name", displayName);
        itemConfig.set(categoryPath + ".icon", icon.name());

        for (ItemStack item : chestSnapInv.getContents()) {
            if (item == null) {
                continue;
            }
            String itemPath = categoryPath + ".item." + item.getType().name();
            itemConfig.set(itemPath + ".jp", getItemName(item, ItemLanguage.JAPANESE_NAME_MAP));
            itemConfig.set(itemPath + ".en", getItemName(item, ItemLanguage.ENGLISH_NAME_MAP));
        }

        CONFIG.getItemCustomConfig().saveConfig();
        CONFIG.addCategory();

        player.sendMessage("新たなカテゴリの追加が完了しました。");
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        if (event.getPlayer() == player) {
            HandlerList.unregisterAll(this);
        }
    }

    private static String getItemName(ItemStack item, Map<String, String> langMap) {
        Material type = item.getType();
        switch (type) {
        case PLAYER_HEAD:
        case PLAYER_WALL_HEAD:
            return getHeadName(item, langMap);
        case TIPPED_ARROW:
            return getTippedArrowName(item, langMap);
        case POTION:
            return getPotionName(item, langMap);
        case SPLASH_POTION:
            return getSplashPotionName(item, langMap);
        case LINGERING_POTION:
            return getLingeringPotionName(item, langMap);
        case SHIELD:
            return getShieldName(item, langMap);
        default:
            return langMap.get(type.name().toLowerCase());
        }
    }

    /**
     * もし渡されたアイテムがplayer_headだった場合、渡されたマップに応じてデフォルトの名前の文字列を取得する。
     * 
     * @param playerHead プレイヤーの頭
     * @param langMap アイテムのデフォルト名を格納したマップ
     * @return プレイヤーの頭の名前
     */
    private static String getHeadName(ItemStack playerHead, Map<String, String> langMap) {
        if (playerHead.getType() != Material.PLAYER_HEAD || playerHead.getType() != Material.PLAYER_WALL_HEAD) {
            return "";
        }

        if (langMap != ItemLanguage.JAPANESE_NAME_MAP && langMap != ItemLanguage.ENGLISH_NAME_MAP) {
            return "";
        }

        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        if (meta == null || !meta.hasOwner()) {
            return langMap.get("player_head");
        }

        OfflinePlayer player = meta.getOwningPlayer();
        if (player == null){
            return langMap.get("player_head");
        }

        String ownerName = meta.getOwningPlayer().getName();
        if (ownerName == null) {
            return langMap.get("player_head");
        }

        return langMap.get("player_head.named").replaceFirst("%s", ownerName);
    }

    /**
     * もし渡されたアイテムがtipped_arrowだった場合、渡されたマップに応じてデフォルトの名前の文字列を取得する。
     * 
     * @param tippedArrow 効果付きの矢
     * @param langMap アイテムのデフォルト名を格納したマップ
     * @return tipped_arrowの効果ごとの名前
     */
    private static String getTippedArrowName(ItemStack tippedArrow, Map<String, String> langMap) {
        if (tippedArrow.getType() != Material.TIPPED_ARROW) {
            return "";
        }
        if (langMap != ItemLanguage.JAPANESE_NAME_MAP && langMap != ItemLanguage.ENGLISH_NAME_MAP) {
            return "";
        }

        PotionMeta meta = (PotionMeta) tippedArrow.getItemMeta();
        if (meta == null){
            return "";
        }
        String effectName = meta.getBasePotionData().getType().name().toLowerCase();
        if (effectName.equals("empty")) {
            return langMap.get("tipped_arrow.effect.empty");
        }

        return langMap.get("tipped_arrow.effect." + effectName);
    }

    /**
     * もし渡されたアイテムがpotionだった場合、渡されたマップに応じてデフォルトの名前の文字列を取得する。
     * 
     * @param potion 普通のポーション
     * @param langMap アイテムのデフォルト名を格納したマップ
     * @return potionの効果ごとの名前
     */
    private static String getPotionName(ItemStack potion, Map<String, String> langMap) {
        if (potion.getType() != Material.POTION) {
            return "";
        }
        if (langMap != ItemLanguage.JAPANESE_NAME_MAP && langMap != ItemLanguage.ENGLISH_NAME_MAP) {
            return "";
        }

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        if (meta == null){
            return "";
        }
        String effectName = meta.getBasePotionData().getType().name().toLowerCase();
        if (effectName.equals("empty")) {
            return langMap.get("potion.effect.empty");
        }

        return langMap.get("potion.effect." + effectName);
    }

    /**
     * もし渡されたアイテムがsplash_potionだった場合、渡されたマップに応じてデフォルトの名前の文字列を取得する。
     * 
     * @param potion スプラッシュポーション
     * @param langMap アイテムのデフォルト名を格納したマップ
     * @return splash_potionの効果ごとの名前
     */
    private static String getSplashPotionName(ItemStack potion, Map<String, String> langMap) {
        if (potion.getType() != Material.SPLASH_POTION) {
            return "";
        }
        if (langMap != ItemLanguage.JAPANESE_NAME_MAP && langMap != ItemLanguage.ENGLISH_NAME_MAP) {
            return "";
        }

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        String effectName = meta.getBasePotionData().getType().name().toLowerCase();
        if (effectName.equals("empty")) {
            return langMap.get("splash_potion.effect.empty");
        }

        return langMap.get("splash_potion.effect." + effectName);
    }

    /**
     * もし渡されたアイテムがsplash_potionだった場合、渡されたマップに応じてデフォルトの名前の文字列を取得する。
     * 
     * @param potion 残留ポーション
     * @param langMap アイテムのデフォルト名を格納したマップ
     * @return splash_potionの効果ごとの名前
     */
    private static String getLingeringPotionName(ItemStack potion, Map<String, String> langMap) {
        if (potion.getType() != Material.LINGERING_POTION) {
            return "";
        }
        if (langMap != ItemLanguage.JAPANESE_NAME_MAP && langMap != ItemLanguage.ENGLISH_NAME_MAP) {
            return "";
        }

        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        String effectName = meta.getBasePotionData().getType().name().toLowerCase();
        if (effectName.equals("empty")) {
            return langMap.get("lingering_potion.effect.empty");
        }

        return langMap.get("lingering_potion.effect." + effectName);
    }

    /**
     * もし渡されたアイテムがshieldだった場合、渡されたマップに応じてデフォルトの名前の文字列を取得する。
     * 
     * @param shield 盾
     * @param langMap アイテムのデフォルト名を格納したマップ
     * @return shieldの色ごとの名前
     */
    private static String getShieldName(ItemStack shield, Map<String, String> langMap) {
        if (shield.getType() != Material.SHIELD) {
            return "";
        }
        if (langMap != ItemLanguage.JAPANESE_NAME_MAP && langMap != ItemLanguage.ENGLISH_NAME_MAP) {
            return "";
        }

        BlockStateMeta meta = (BlockStateMeta) shield.getItemMeta();
        if (!meta.hasBlockState()) {
            return langMap.get("shield");
        }

        Banner banner = (Banner) meta.getBlockState();
        String color = banner.getBaseColor().name().toLowerCase();

        return langMap.get("shield." + color);
    }
}