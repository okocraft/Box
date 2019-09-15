package net.okocraft.box.database;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//((.*)\(.*\),) (.*)
//$1\n    $3

public final class Items {

    @NotNull
    private static Set<String> items = getItems();

    private Items() {}

    /**
     * すべてのデータベース上に格納されるアイテムを含むSetを取得する。
     * 
     * @return
     */
    @NotNull
    public static Set<String> getItems() {
        if (items != null) {
            return Collections.unmodifiableSet(items);
        }
        items = new LinkedHashSet<>() {
            private static final long serialVersionUID = 1L;
            {
                for (Material item : Material.values()) {
                    switch (item) {
                    case POTION:
                    case SPLASH_POTION:
                    case LINGERING_POTION:
                    case TIPPED_ARROW:
                        String potionItemName = item.name();
                        for (PotionType potionType : PotionType.values()) {
                            String potionTypeName = potionType.name();
                            if (potionType.isExtendable()) {
                                add(potionItemName + "_" + potionTypeName + "_EXTENDED");
                            }

                            if (potionType.isUpgradeable()) {
                                add(potionItemName + "_" + potionTypeName + "_UPGRADED");
                            }

                            add(potionItemName + "_" + potionTypeName);

                        }
                    default:
                        add(item.name());
                    }
                }
            }
        };

        return Collections.unmodifiableSet(items);
    }

    /**
     * 指定したItemStackのデータベース上の名前を取得する。
     * 
     * @param item
     * @param ignoreMeta falseの場合、メタを持っているアイテムを指定するとnullを返すようになる。
     * @return 名前 または null
     */
    @Nullable
    public static String getName(@NotNull ItemStack item, boolean ignoreMeta) {
        Material type = item.getType();
        switch (type) {
        case POTION:
        case SPLASH_POTION:
        case LINGERING_POTION:
        case TIPPED_ARROW:
            PotionData data = ((PotionMeta) item.getItemMeta()).getBasePotionData();

            PotionType potionType = data.getType();

            if (potionType == PotionType.UNCRAFTABLE) {
                if (ignoreMeta) {
                    return type.name();
                }
                return item.hasItemMeta() ? null : type.name();
            }

            boolean extended = data.isExtended();
            boolean upgraded = data.isUpgraded();

            if (extended && upgraded) {
                return null;
            }

            String extendedPart = data.isExtended() ? "_EXTENDED" : "";
            String upgradedPart = data.isUpgraded() ? "_UPGRADED" : "";
            String name = type.name() + "_" + potionType.name() + extendedPart + upgradedPart;

            if (ignoreMeta) {
                return name;
            }
            return getItemStack(name).getItemMeta().equals(item.getItemMeta()) ? name : null;

        default:
            if (ignoreMeta) {
                return type.name();
            }
            return item.hasItemMeta() ? null : type.name();
        }
    }

    @Nullable
    public static ItemStack getItemStack(@NotNull String itemName) {
        try {
            return new ItemStack(Material.valueOf(itemName));
        } catch (IllegalArgumentException e) {

            Material potion;
            if (itemName.startsWith("POTION")) {
                potion = Material.POTION;
            } else if (itemName.startsWith("SPLASH_POTION")) {
                potion = Material.SPLASH_POTION;
            } else if (itemName.startsWith("LINGERING_POTION")) {
                potion = Material.LINGERING_POTION;
            } else if (itemName.startsWith("TIPPED_ARROW")) {
                potion = Material.TIPPED_ARROW;
            } else {
                return null;
            }

            String potionData = itemName.substring(potion.name().length() + 1);
            boolean isExtended = potionData.endsWith("_EXTENDED");
            boolean isUpgraded = potionData.endsWith("_UPGRADED");

            if (isExtended || isUpgraded) {
                // trim _EXTEFNDED or _UPGRADED
                potionData = potionData.substring(0, potionData.length() - 9);
            }

            PotionType potionType;
            try {
                potionType = PotionType.valueOf(potionData);
            } catch (IllegalArgumentException _e) {
                return null;
            }

            return createPotion(potion, potionType, isExtended, isUpgraded);
        }
    }

    public static boolean contains(String name) {
        return items.contains(name);
    }

    @org.jetbrains.annotations.Nullable
    private static ItemStack createPotion(@NotNull Material potionItem, @NotNull PotionType type, boolean extended, boolean upgraded) {
        ItemStack potion = new ItemStack(potionItem);
        if (potion.getItemMeta() instanceof PotionMeta) {

            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.setBasePotionData(new PotionData(type, extended, upgraded));
            potion.setItemMeta(meta);
            return potion;
        }
        return null;
    }
}