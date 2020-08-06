package net.okocraft.box.plugin.category;

import net.okocraft.box.plugin.model.item.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * 一つのカテゴリを管理するクラス。
 */
public class Category {

    private final String name;
    private final String displayName;
    private final ItemStack icon;
    private final List<Item> items;

    public Category(@NotNull String name, @NotNull String displayName,
                    @NotNull ItemStack icon, @NotNull List<Item> items) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.items = items;
    }

    /**
     * カテゴリの名前を返す。
     * <p>
     * このメソッドで返される文字列は、{@link net.okocraft.box.plugin.config.CategoryConfig} でルートパスとして使用される。
     *
     * @return カテゴリの名前
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * カテゴリの表示名を返す。
     *
     * @return カテゴリの表示名
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * カテゴリのアイコンを返す。
     *
     * @return カテゴリのアイコン
     */
    @NotNull
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * カテゴリに含まれる {@link Item} を返す。
     * <p>
     * このメソッドで返されるリストは変更可能であるが、そのリストで要素を追加 / 削除 しても
     * {@link net.okocraft.box.plugin.config.CategoryConfig} には保存されない。
     *
     * @return カテゴリに含まれる {@link Item}
     */
    @NotNull
    public List<Item> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Category)) {
            return false;
        }

        Category category = (Category) o;
        return Objects.equals(name, category.name) && Objects.equals(items, category.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, items);
    }
}
