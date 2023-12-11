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

import java.util.List;

public class IncreaseCustomNumberButton extends AmountModificationButton {

    private static final SoundBase INCREASE_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON).pitch(1.5f).build();

    private final Component displayName;
    private final SingleArgument<Integer> clickToSetLore;
    private final SingleArgument<Integer> clickToIncreaseLore;
    private final SingleArgument<Integer> currentAmountLore;
    private final ClickResult returningResult;

    public IncreaseCustomNumberButton(int slot, @NotNull TypedKey<Amount> dataKey,
                                      @NotNull Component displayName, @NotNull SingleArgument<Integer> clickToSetLore,
                                      @NotNull SingleArgument<Integer> clickToIncreaseLore, @NotNull SingleArgument<Integer> currentAmountLore,
                                      @NotNull ClickResult returningResult) {
        super(slot, dataKey);
        this.displayName = displayName;
        this.clickToSetLore = clickToSetLore;
        this.clickToIncreaseLore = clickToIncreaseLore;
        this.currentAmountLore = currentAmountLore;
        this.returningResult = returningResult;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);

        icon.editMeta(meta -> {
            var viewer = session.getViewer();

            meta.displayName(TranslationUtil.render(displayName, viewer));

            var amount = getOrCreateAmount(session);
            var unit = amount.getUnit().getAmount();
            var currentAmount = amount.getValue();

            meta.lore(TranslationUtil.render(
                    List.of(
                            Component.empty(),
                            getClickToLore(unit, currentAmount).apply(unit),
                            Component.empty(),
                            currentAmountLore.apply(currentAmount)
                    ),
                    viewer
            ));
        });
        return icon;
    }

    private @NotNull SingleArgument<Integer> getClickToLore(int unit, int amount) {
        return unit != 1 && amount == 1 ? clickToSetLore : clickToIncreaseLore;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var amount = getOrCreateAmount(session);
        var unit = amount.getUnit();
        var current = amount.getValue();

        if (current == 1 && unit.getAmount() != 1) {
            amount.setValue(unit.getAmount());
        } else {
            amount.increase();
        }

        INCREASE_SOUND.play(session.getViewer());

        return returningResult;
    }
}
