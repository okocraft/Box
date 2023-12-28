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

public class DecreaseAmountButton extends AmountModificationButton {

    private static final SoundBase DECREASE_SOUND = SoundBase.builder().sound(Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF).pitch(1.5f).build();

    private final MiniMessageBase displayName;
    private final Arg1<Integer> clickToDecreaseLore;
    private final Arg1<Integer> currentAmountLore;
    private final ClickResult returningResult;

    public DecreaseAmountButton(int slot, @NotNull TypedKey<Amount> dataKey,
                                @NotNull MiniMessageBase displayName,
                                @NotNull Arg1<Integer> clickToDecreaseLore,
                                @NotNull Arg1<Integer> currentAmountLore,
                                @NotNull ClickResult returningResult) {
        super(slot, dataKey);
        this.displayName = displayName;
        this.clickToDecreaseLore = clickToDecreaseLore;
        this.currentAmountLore = currentAmountLore;
        this.returningResult = returningResult;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var amount = this.getOrCreateAmount(session);

        return ItemEditor.create()
                .displayName(this.displayName.create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine(this.clickToDecreaseLore.apply(amount.getUnit().getAmount()).create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine(this.currentAmountLore.apply(amount.getValue()).create(session.getMessageSource()))
                .createItem(Material.RED_STAINED_GLASS_PANE);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var amount = this.getOrCreateAmount(session);
        var current = amount.getValue();

        if (1 < current) {
            amount.decrease();
        } else {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        DECREASE_SOUND.play(session.getViewer());

        return this.returningResult;
    }
}
