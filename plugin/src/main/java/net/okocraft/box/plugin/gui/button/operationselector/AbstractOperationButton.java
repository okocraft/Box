package net.okocraft.box.plugin.gui.button.operationselector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.gui.button.AbstractButton;
import net.okocraft.box.plugin.gui.button.ButtonIcon;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.sound.SoundPlayer;

public abstract class AbstractOperationButton extends AbstractButton {
    
    protected static final SoundPlayer SOUND_PLAYER = PLUGIN.getSoundPlayer();

    protected final User user;
    protected final Item item;

    protected int quantity;

    public AbstractOperationButton(@NotNull ButtonIcon icon, @NotNull User user, @NotNull Item item) {
        super(icon);
        this.item = item;
        this.user = user;
    }

    private static int firstPartial(Inventory inv, ItemStack item) {
        if (item == null) {
            return -1;
        }
        ItemStack[] inventory = inv.getStorageContents();
        ItemStack filteredItem = item.clone();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack cItem = inventory[i];
            if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(filteredItem)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * アイテムスタックの限界サイズを超えないように渡されたアイテムをインベントリに格納する。
     * {@link org.bukkit.inventory.Inventory#addItem(ItemStack...)}はスタックの限界サイズを無視してしまう。
     * 
     * @param inv アイテムを格納するインベントリ
     * @param items 格納するアイテムの配列
     * @return 格納できなかったアイテムスタックとそのアイテムスタックの配列のインデックスのマップ
     */
    protected static Map<Integer, ItemStack> safeAddItem(Inventory inv, ItemStack... items) {
        Objects.requireNonNull(items, "Item cannot be null");
        Map<Integer, ItemStack> leftover = new HashMap<Integer, ItemStack>();

        /* TODO: some optimization
         *  - Create a 'firstPartial' with a 'fromIndex'
         *  - Record the lastPartial per Material
         *  - Cache firstEmpty result
         */

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            while (true) {
                // Do we already have a stack of it?
                
                int firstPartial = firstPartial(inv, item);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    int firstFree = inv.firstEmpty();

                    if (firstFree == -1) {
                        // No space at all!
                        leftover.put(i, item);
                        break;
                    } else {
                        // More than a single stack!
                        if (item.getAmount() > item.getMaxStackSize()) {
                            ItemStack stack = item.clone();
                            stack.setAmount(item.getMaxStackSize());
                            inv.setItem(firstFree, stack);
                            item.setAmount(item.getAmount() - item.getMaxStackSize());
                        } else {
                            // Just store it
                            inv.setItem(firstFree, item);
                            break;
                        }
                    }
                } else {
                    // So, apparently it might only partially fit, well lets do just that
                    ItemStack partialItem = inv.getItem(firstPartial);

                    int amount = item.getAmount();
                    int partialAmount = partialItem.getAmount();
                    int maxAmount = partialItem.getMaxStackSize();

                    // Check if it fully fits
                    if (amount + partialAmount <= maxAmount) {
                        partialItem.setAmount(amount + partialAmount);
                        // To make sure the packet is sent to the client
                        inv.setItem(firstPartial, partialItem);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(maxAmount);
                    // To make sure the packet is sent to the client
                    inv.setItem(firstPartial, partialItem);
                    item.setAmount(amount + partialAmount - maxAmount);
                }
            }
        }
        return leftover;
    }

    @Override
    public void update() {
        //TODO: loreなどをストックに合わせて修正する。
        getIcon().setLore(List.of(""));
    }
}
