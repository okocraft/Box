package net.okocraft.box.plugin.gui.item;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public enum MenuIcon {
    PREV_PAGE("previous-page", Material.ARROW, "&6前のページ", Collections.emptyList(), false),
    NEXT_PAGE("next-page", Material.ARROW, "&6次のページ", Collections.emptyList(), false)
    ;

    private final String rootPath;
    private final Material defMaterial;
    private final String defName;
    private final List<String> defLore;
    private final boolean isGlowing;

    MenuIcon(@NotNull String rootPath, @NotNull Material defMaterial, @Nullable String defName,
             @NotNull List<String> defLore, boolean isGlowing) {
        this.rootPath = rootPath;
        this.defMaterial = defMaterial;
        this.defName = defName;
        this.defLore = defLore;
        this.isGlowing = isGlowing;
    }

    @NotNull
    public String getRootPath() {
        return rootPath;
    }

    @NotNull
    public Material getDefaultMaterial() {
        return defMaterial;
    }

    @Nullable
    public String getDefaultName() {
        return defName;
    }

    @NotNull
    public List<String> getDefaultLore() {
        return defLore;
    }

    public boolean isGlowing() {
        return isGlowing;
    }
}
