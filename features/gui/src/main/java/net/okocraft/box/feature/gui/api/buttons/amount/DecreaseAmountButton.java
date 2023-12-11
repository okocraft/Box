package net.okocraft.box.feature.gui.api.buttons.amount;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DecreaseAmountButton extends AmountModificationButton {

    private static final SoundBase DECREASE_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF).pitch(1.5f).build();

    private final Component displayName;
    private final SingleArgument<Integer> clickToDecreaseLore;
    private final SingleArgument<Integer> currentAmountLore;
    private final ClickResult returningResult;

    public DecreaseAmountButton(int slot, @NotNull TypedKey<Amount> dataKey,
                                @NotNull Component displayName,
                                @NotNull SingleArgument<Integer> clickToDecreaseLore,
                                @NotNull SingleArgument<Integer> currentAmountLore,
                                @NotNull ClickResult returningResult) {
        super(slot, dataKey);
        this.displayName = displayName;
        this.clickToDecreaseLore = clickToDecreaseLore;
        this.currentAmountLore = currentAmountLore;
        this.returningResult = returningResult;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(Material.RED_STAINED_GLASS_PANE);

        icon.editMeta(meta -> {
            var viewer = session.getViewer();
            meta.displayName(TranslationUtil.render(displayName, viewer));

            var lore = new ArrayList<Component>();

            var amount = getOrCreateAmount(session);
            var unit = amount.getUnit().getAmount();
            var currentAmount = amount.getValue();

            lore.add(Component.empty());

            lore.add(clickToDecreaseLore.apply(unit));


            lore.add(Component.empty());

            lore.add(currentAmountLore.apply(currentAmount));

            meta.lore(TranslationUtil.render(lore, viewer));
        });

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var amount = getOrCreateAmount(session);
        var current = amount.getValue();

        if (1 < current) {
            amount.decrease();
        } else {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        DECREASE_SOUND.play(session.getViewer());

        return returningResult;
    }
}
