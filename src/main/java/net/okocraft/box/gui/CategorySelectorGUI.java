package net.okocraft.box.gui;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import lombok.val;
import net.okocraft.box.Box;
import net.okocraft.box.util.GeneralConfig;

public class CategorySelectorGUI implements Listener {

    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();

    private static final NamespacedKey CATEGORY_SELECTOR_KEY = new NamespacedKey(INSTANCE, "categoryselector");
    private static final NamespacedKey CATEGORY_NAME_KEY = new NamespacedKey(INSTANCE, "categoryname");

    public static final Inventory GUI = Bukkit.createInventory(null, 54, CONFIG.getCategorySelectionGuiName());

    private static List<Integer> flameSlots = List.of(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
    );

    private static CategorySelectorGUI categorySelector;

    /**
     * コンストラクタ
     */
    private CategorySelectorGUI() {
        initGUI();
    }

    public static void initGUI() {
        GUI.clear();
        ItemStack flame = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta flameMeta = flame.getItemMeta();
        flameMeta.setDisplayName("§r");
        flameMeta.getPersistentDataContainer().set(CATEGORY_SELECTOR_KEY, PersistentDataType.INTEGER, 1);
        flame.setItemMeta(flameMeta);
        flameSlots.forEach(slot -> GUI.setItem(slot, flame));

        List<ItemStack> itemList = CONFIG.getCategories().entrySet().stream().map(entry -> createItem(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        GUI.addItem(itemList.toArray(new ItemStack[itemList.size()]));
    }
    
    /**
     * categoryのアイコンなどの情報をsectionから引き出し、そのアイテムを作る。
     * 
     * @param categoryName
     * @param section
     * @return アイテム
     */
    private static ItemStack createItem(String categoryName, ConfigurationSection section) {
        Material categoryIconMaterial = Material
                .valueOf(Optional.ofNullable(section.getString("icon")).orElse("BARRIER").toUpperCase());
        ItemStack categorySelectionItem = new ItemStack(categoryIconMaterial);
        Optional<ItemMeta> categorySelectionItemMeta = Optional.ofNullable(categorySelectionItem.getItemMeta());
        // setDisplayNameにnullを渡すために、存在しない場合は素直にnullを吐かせている。
        String categoryItemName = Optional.ofNullable(section.getString("display_name"))
                .map(name -> name.replaceAll("&([a-f0-9])", "§$1")).orElse(null);

        categorySelectionItemMeta.ifPresent(meta -> {
            meta.setDisplayName(categoryItemName);
            meta.getPersistentDataContainer().set(CATEGORY_NAME_KEY, PersistentDataType.STRING, categoryName);

            categorySelectionItem.setItemMeta(meta);
        });

        return categorySelectionItem;
    }

    /**
     * リスナーを動かす。カテゴリーGUIと違ってカテゴリー選択GUIはonEnableのときから常にリスナーをオンにしておく。
     */
    public static void startListener() {
        if (categorySelector != null) {
            return;
        }
        categorySelector = new CategorySelectorGUI();
        Bukkit.getPluginManager().registerEvents(categorySelector, INSTANCE);
    }

    /**
     * リスナーを止める。
     */
    public static void stopListener() {
        if (categorySelector == null) {
            return;
        }
        HandlerList.unregisterAll(categorySelector);
        categorySelector = null;
    }

    /**
     * リスナーを再起動する。
     */
    public static void restartListener() {
        stopListener();
        startListener();
    }

    /**
     * カテゴリ選択GUIへのクリックを検知して、適切なカテゴリGUIに遷移させる。
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        val player = (Player) event.getWhoClicked();
        val action = event.getAction();
        val inventory = event.getClickedInventory();
        if (inventory == null || inventory.getItem(0) == null || !GUI.getItem(0).isSimilar(inventory.getItem(0))) {
            return;
        }
        event.setCancelled(true);
        
        if (CONFIG.getDisabledWorlds().contains(event.getWhoClicked().getWorld())) {
            return;
        }

        int clickedSlot = event.getSlot();
        val clickedItem = inventory.getItem(clickedSlot);

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (action == InventoryAction.NOTHING || inventory.getType() == InventoryType.PLAYER) {
            return;
        }

        if (flameSlots.contains(clickedSlot)) {
            return;
        }

        // NOTE: NPE はItemStackがAIRの場合のみしか起こらない。
        val categoryName = clickedItem.getItemMeta().getPersistentDataContainer()
                .get(CATEGORY_NAME_KEY, PersistentDataType.STRING);

        player.closeInventory();

        new CategoryGUI(player, categoryName, 1);

        return;
    }
}