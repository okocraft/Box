package net.okocraft.box.plugin.gui;

import net.okocraft.box.plugin.gui.button.Button;
import net.okocraft.box.plugin.gui.button.NextPageButton;
import net.okocraft.box.plugin.gui.button.PreviousPageButton;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class BoxInventoryHolder implements InventoryHolder {

    protected final Inventory inv;
    protected final MenuType menuType;
    protected final PaginatedButtonList buttonList = new PaginatedButtonList();
    protected int page = 1;

    public BoxInventoryHolder(MenuType type, @NotNull String title) {
        this.menuType = type;
        this.inv = Bukkit.createInventory(this, type.getSize(), title);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    public void setPage(int page) {
        int newPage = Math.max(page, 1);
        if (page != newPage) {
            this.page = newPage;
            setItems();
        }
    }

    public int getPage() {
        return page;
    }

    public void setItem(int slot) {
        if (0 <= slot && slot <= inv.getSize()) {
            Button button = buttonList.getButton(page, slot);
            inv.setItem(slot, button != null ? button.getIcon().getItemStack() : null);
        }
    }

    public void setItems() {
        Map<Integer, Button> pageButtons = buttonList.getPageButtons(page);
        for (int slot = 0; slot < inv.getSize(); slot++) {
            Button button = pageButtons.get(slot);
            inv.setItem(slot, button != null ? button.getIcon().getItemStack() : null);
        }
    }

    public void onClick(@NotNull InventoryClickEvent e) {
        Button button = buttonList.getPageButtons(page).get(e.getSlot());
        if (button != null) {
            button.onClick(e);
        }
    }

    public void update() {
        buttonList.getAllButtons().forEach(Button::update);
        setItems();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BoxInventoryHolder)) {
            return false;
        }
        BoxInventoryHolder boxInventoryHolder = (BoxInventoryHolder) o;
        return Objects.equals(menuType, boxInventoryHolder.menuType) && Objects.equals(buttonList, boxInventoryHolder.buttonList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuType, buttonList);
    }

    protected void putElementAndPageArrow(List<Button> elements) {
        int page = 1;
        int slot = 0;
        if (inv.getSize() > 9) {
            for (Button button : elements) {
                if (slot >= inv.getSize() - 9) {
                    slot = 0;
                    page++;
                }
                
                buttonList.putButton(page, slot, button);
                
                slot++;
            }
        } else {
            slot++;
            for (Button button : elements) {
                if (slot == 8) {
                    slot = 1;
                    page++;
                }
                
                buttonList.putButton(page, slot, button);
                
                slot++;
            }
        }

        Button prevPageIcon = new PreviousPageButton();
        Button nextPageIcon = new NextPageButton();
        for (int currentPage = 1; currentPage <= page; currentPage++) {
            buttonList.putButton(currentPage, inv.getSize() - 9, prevPageIcon);
            buttonList.putButton(currentPage, inv.getSize() - 1, nextPageIcon);
        }
    }
}
