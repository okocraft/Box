package net.okocraft.box.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 取引量、前のメニューへの帰還、そしてページ変更ボタンを追加したGUIの実装
 */
abstract class BackMenuButtomGUI extends PagedGUI {

    private final int backMenuSlot;

    /**
     * コンストラクタ
     * 
     * @param player           GUIを開いているプレイヤー
     * @param guiTitle         GUIのタイトル
     * @param GUISize          GUIのサイズ
     * @param previousPageSlot 前ページに戻るアイテムのスロット
     * @param nextPageSlot     次のページに進むアイテムのスロット
     * @param backMenuSlot     前のメニューに戻るアイテムのスロット
     */
    BackMenuButtomGUI(Player player, String guiTitle, int GUISize, int previousPageSlot, int nextPageSlot,
            int backMenuSlot) {
        super(player, guiTitle, GUISize, previousPageSlot, nextPageSlot);

        this.backMenuSlot = backMenuSlot;

        @SuppressWarnings("serial")
        Map<Integer, ItemStack> pageCommonItems = new HashMap<>() {
            {
                put(backMenuSlot, layout.getBackMenu());
            }
        };
        putPageCommonItems(pageCommonItems);
    }

    @Override
    public void onClicked(InventoryClickEvent event) {
        if (event.getSlot() == backMenuSlot) {
            config.playBackMenuSound(getPlayer());
            getPlayer().openInventory(new CategorySelectorGUI().getInventory());
            return;
        }

        super.onClicked(event);
    }
}