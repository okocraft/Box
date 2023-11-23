package net.okocraft.box.version.common.item;

import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

class DefaultEnchantedBooksIterator extends DefaultItemIterator {

    private static final Enchantment[] ENCHANTMENTS = Enchantment.values();

    private final EnchantmentStorageMeta enchantmentStorageMeta = this.createItemMeta(Material.ENCHANTED_BOOK, EnchantmentStorageMeta.class);

    private int enchantmentIndex = 0;

    @Override
    public boolean hasNext() {
        return this.enchantmentIndex != ENCHANTMENTS.length;
    }

    @Override
    public @NotNull DefaultItem next() {
        var enchant = ENCHANTMENTS[this.enchantmentIndex++];
        this.enchantmentStorageMeta.addStoredEnchant(enchant, enchant.getMaxLevel(), false);

        var name = Material.ENCHANTED_BOOK.name() + "_" + enchant.getKey().getKey().toUpperCase(Locale.ROOT);

        var book = new ItemStack(Material.ENCHANTED_BOOK, 0);
        book.setItemMeta(this.enchantmentStorageMeta);

        return new DefaultItem(name, book);
    }
}
