package net.okocraft.box.feature.gui.api.buttons.amount;

import dev.siroshun.mcmsgdef.MessageKey;
import io.papermc.paper.registry.keys.SoundEventKeys;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DecreaseAmountButton extends AmountModificationButton {

    private static final SoundBase DECREASE_SOUND = SoundBase.builder().sound(SoundEventKeys.BLOCK_WOODEN_BUTTON_CLICK_OFF).pitch(1.5f).build();

    private final MessageKey displayName;
    private final MessageKey.Arg1<Integer> clickToDecreaseLore;
    private final MessageKey.Arg1<Integer> currentAmountLore;
    private final ClickResult returningResult;

    public DecreaseAmountButton(int slot, @NotNull TypedKey<Amount> dataKey,
                                @NotNull MessageKey displayName,
                                @NotNull MessageKey.Arg1<Integer> clickToDecreaseLore,
                                @NotNull MessageKey.Arg1<Integer> currentAmountLore,
                                @NotNull ClickResult returningResult) {
        super(slot, dataKey);
        this.displayName = displayName;
        this.clickToDecreaseLore = clickToDecreaseLore;
        this.currentAmountLore = currentAmountLore;
        this.returningResult = returningResult;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        Amount amount = this.getOrCreateAmount(session);

        return ItemEditor.create()
            .displayName(this.displayName)
            .loreEmptyLine()
            .loreLine(this.clickToDecreaseLore.apply(amount.getUnit().getAmount()))
            .loreEmptyLine()
            .loreLine(this.currentAmountLore.apply(amount.getValue()))
            .createItem(session.getViewer(), Material.RED_STAINED_GLASS_PANE);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        Amount amount = this.getOrCreateAmount(session);
        int current = amount.getValue();

        if (1 < current) {
            amount.decrease();
        } else {
            return ClickResult.NO_UPDATE_NEEDED;
        }

        DECREASE_SOUND.play(session.getViewer());

        return this.returningResult;
    }
}
