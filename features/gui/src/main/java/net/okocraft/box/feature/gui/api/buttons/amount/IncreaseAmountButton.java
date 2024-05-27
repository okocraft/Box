package net.okocraft.box.feature.gui.api.buttons.amount;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IncreaseAmountButton extends AmountModificationButton {

    private static final SoundBase INCREASE_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON).pitch(1.5f).build();

    private final MiniMessageBase displayName;
    private final Arg1<Integer> clickToSetLore;
    private final Arg1<Integer> clickToIncreaseLore;
    private final Arg1<Integer> currentAmountLore;
    private final ClickResult returningResult;

    public IncreaseAmountButton(int slot, @NotNull TypedKey<Amount> dataKey,
                                @NotNull MiniMessageBase displayName,
                                @NotNull Arg1<Integer> clickToSetLore,
                                @NotNull Arg1<Integer> clickToIncreaseLore,
                                @NotNull Arg1<Integer> currentAmountLore,
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
        var amount = this.getOrCreateAmount(session);
        var unit = amount.getUnit().getAmount();
        var currentAmount = amount.getValue();

        return ItemEditor.create()
                .displayName(this.displayName.create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine(this.getClickToLore(unit, currentAmount).apply(unit).create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine(this.currentAmountLore.apply(currentAmount).create(session.getMessageSource()))
                .createItem(Material.BLUE_STAINED_GLASS_PANE);
    }

    private @NotNull Arg1<Integer> getClickToLore(int unit, int amount) {
        return unit != 1 && amount == 1 ? this.clickToSetLore : this.clickToIncreaseLore;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var amount = this.getOrCreateAmount(session);
        var unit = amount.getUnit();
        var current = amount.getValue();

        if (current == 1 && unit.getAmount() != 1) {
            amount.setValue(unit.getAmount());
        } else {
            amount.increase();
        }

        INCREASE_SOUND.play(session.getViewer());

        return this.returningResult;
    }
}
