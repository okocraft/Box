package net.okocraft.box.plugin.category;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.config.CategoryConfig;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * カテゴリーを管理するクラス。
 */
public class CategoryManager {

    private final Box plugin;
    private final List<Category> categories = new LinkedList<>();

    public CategoryManager(@NotNull Box plugin) {
        this.plugin = plugin;
    }

    /**
     * カテゴリーを {@link CategoryConfig} から読み込む。
     *
     * @throws IllegalStateException category.yml が読み込めない場合
     */
    public void load() {
        if (!categories.isEmpty()) {
            categories.clear();
        }

        CategoryConfig config = new CategoryConfig(plugin);

        if (!config.isLoaded()) {
            throw new IllegalStateException("Could not load category.yml");
        }

        for (String category : config.getCategories()) {
            String displayName = config.getDisplayName(category);

            ItemStack icon =
                    new ItemBuilder()
                            .setMaterial(config.getIconMaterial(category))
                            .setDisplayName(displayName)
                            .build();

            List<Item> items = config.getItems(category);

            categories.add(new Category(category, displayName, icon, items));
        }
    }

    /**
     * 登録済みのカテゴリーのリストをコピーして返す。
     * <p>
     * このメソッドで返されるリストは変更不可能である。
     *
     * @return 登録済みのカテゴリーリスト
     * @see List#copyOf(Collection)
     */
    @NotNull
    @Unmodifiable
    public List<Category> getCategories() {
        return List.copyOf(categories);
    }

    /**
     * カテゴリーを名前から取得する。
     *
     * @param name カテゴリーの名前
     * @return 名前からカテゴリーを検索した結果
     */
    @NotNull
    public Optional<Category> getCategory(@NotNull String name) {
        return categories.stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    /**
     * 渡した {@link Item} が属するカテゴリーを返す。
     * <p>
     * 複数のカテゴリーに属していても、最初に見つかったカテゴリーを返す。
     *
     * @param item 検索する {@link Item}
     * @return {@link Item} を含むカテゴリーを検索した結果
     */
    @NotNull
    public Optional<Category> getCategory(@NotNull Item item) {
        return categories.stream().filter(c -> c.getItems().contains(item)).findFirst();
    }

    /**
     * カテゴリーを追加する。
     * <p>
     * このメソッドでは {@link CategoryConfig} への保存は行われない。
     *
     * @param name     カテゴリーの名前
     * @param material カテゴリーのアイコンの {@link Material}
     * @param items    カテゴリーに含まれる {@link Item}
     */
    public void addCategory(@NotNull String name, @NotNull Material material, @NotNull List<Item> items) {
        ItemStack icon = new ItemBuilder().setMaterial(material).setDisplayName(name).build();

        Category category = new Category(name, name, icon, items);

        categories.add(category);
    }

    /**
     * 現在のカテゴリー構成を保存する。
     * <p>
     * このメソッドは非同期で実行するべきである。
     *
     * @throws IllegalStateException category.yml に保存できない場合
     */
    public void save() {
        CategoryConfig config = new CategoryConfig(plugin);

        List.copyOf(categories).forEach(config::setCategory);

        if (!config.save()) {
            throw new IllegalStateException("Could not save category.yml");
        }
    }
}
