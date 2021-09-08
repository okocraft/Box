package net.okocraft.box.category;

import net.okocraft.box.category.model.Category;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CategoryHolder {

    private static final @NotNull List<Category> CATEGORIES = new ArrayList<>();

    public static @NotNull List<Category> get() {
        return CATEGORIES;
    }

    static void addAll(@NotNull List<Category> categoryList) {
        CATEGORIES.addAll(categoryList);
    }
}
