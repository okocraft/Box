package net.okocraft.box.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface Clickable {

    /**
     * アイテムがクリックされた場合に呼び出されるメソッド。担当するGUIに属するするアイテムの処理のみを記述することが望ましい。
     */
    void onClicked(InventoryClickEvent event);
}