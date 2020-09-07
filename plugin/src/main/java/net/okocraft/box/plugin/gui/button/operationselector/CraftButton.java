package net.okocraft.box.plugin.gui.button.operationselector;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.config.RecipeConfig;
import net.okocraft.box.plugin.gui.BoxInventoryHolder;
import net.okocraft.box.plugin.gui.button.ButtonIcon;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.BoxRecipe;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.sound.BoxSound;

public class CraftButton extends AbstractOperationButton {

    private final static RecipeConfig RECIPE_CONFIG = PLUGIN.getRecipeConfig();

    private final BoxRecipe recipe;

    public CraftButton(@NotNull User user, @NotNull Item item) throws IllegalArgumentException {
        super(new ButtonIcon(item.getOriginalCopy()), user, item);

        this.recipe = RECIPE_CONFIG.getCustomRecipe(item.getOriginalCopy()).orElse(BoxRecipe.of(item));
        if (recipe == null) {
            throw new IllegalArgumentException("The item is not craftable.");
        }

        icon.applyConfig("craft-element");
        //TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        if (craft() != 0) {
            SOUND_PLAYER.play((Player) e.getWhoClicked(), BoxSound.ITEM_NOT_ENOUGH);
            return;
        }

        SOUND_PLAYER.play((Player) e.getWhoClicked(), BoxSound.ITEM_CRAFT);
        update();
        ((BoxInventoryHolder) e.getInventory().getHolder()).setItem(e.getSlot());
    }

    /**
     * アイテムをクラフトする。引数にはGUIでクリックしたアイテムを取る。
     * 
     * @param item 作ろうとしているアイテムのGUI表示
     * @return 作られたアイテムの数
     */
    private int craft() {
        int frequency = quantity;
        for (Map.Entry<Item, Integer> entry : recipe.getIngredients().entrySet()) {
            // 材料の在庫から、作れるアイテム数を割り出す
            int materialStock = user.getAmount(entry.getKey());
            frequency = Math.min(entry.getValue() * frequency, materialStock) / entry.getValue();
        }
        if (frequency == 0) {
            return 0;
        }
        for (Map.Entry<Item, Integer> entry : recipe.getIngredients().entrySet()) {
            user.decrease(entry.getKey(), frequency * entry.getValue());
        }
        int add = frequency * recipe.getResult().getAmount();
        user.increase(item, add);
        return add;
    }

    @Override
    public void update() {
        //TODO: loreなどをストックに合わせて修正する。
        getIcon().setLore(List.of(""));
    }
}
