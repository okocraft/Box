package net.okocraft.box.plugin.sound;

import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

public enum BoxSound {

    MENU_OPEN("menu-open", Sound.BLOCK_CHEST_OPEN),
    MENU_PAGE_CHANGE("menu-page-change", Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
    MENU_PAGE_BACK("menu-page-back", Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
    MENU_CLOSE("menu-close", Sound.BLOCK_CHEST_CLOSE),
    ITEM_DEPOSIT("item-deposit", Sound.ENTITY_ITEM_PICKUP),
    ITEM_WITHDRAW("item-withdraw", Sound.BLOCK_STONE_BUTTON_CLICK_ON),
    ITEM_BUY("item-buy", Sound.ENTITY_ITEM_PICKUP),
    ITEM_SELL("item-sell", Sound.BLOCK_STONE_BUTTON_CLICK_ON),
    ITEM_CRAFT("item-craft", Sound.BLOCK_ANVIL_PLACE),
    ITEM_NOT_ENOUGH("item-not-enough", Sound.ENTITY_ENDERMAN_TELEPORT),
    UNIT_DECREASE("unit-decrease", Sound.BLOCK_TRIPWIRE_CLICK_OFF),
    UNIT_INCREASE("unit-increase", Sound.BLOCK_TRIPWIRE_CLICK_ON),

    ;

    private final String path;
    private final Sound def;
    // private final boolean random;

    BoxSound(@NotNull String path, @NotNull Sound def /*, boolean random*/) {
        this.path = path;
        this.def = def;
        // this.random = random;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public Sound getDef() {
        return def;
    }
}