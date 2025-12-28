package net.okocraft.box.feature.gui.api.buttons.amount;

import dev.siroshun.mcmsgdef.MessageKey;
import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UnitChangeButton extends AmountModificationButton {

    private static final SoundBase RESET_SOUND = SoundBase.builder().sound(SoundEventKeys.ENTITY_EXPERIENCE_ORB_PICKUP).pitch(1.5f).build();

    private final MessageKey displayName;
    private final MessageKey clickToResetAmount;
    private final ClickResult returningResult;

    public UnitChangeButton(int slot, @NotNull TypedKey<Amount> dataKey,
                            @NotNull MessageKey displayName,
                            @NotNull MessageKey clickToResetAmount,
                            @NotNull ClickResult returningResult) {
        super(slot, dataKey);

        this.displayName = displayName;
        this.clickToResetAmount = clickToResetAmount;
        this.returningResult = returningResult;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var editor = ItemEditor.create().displayName(this.displayName);
        var currentUnit = this.getOrCreateAmount(session).getUnit();

        for (var unit : Amount.Unit.values()) {
            editor.loreLine(
                Component.text()
                    .content(" > " + unit.getAmount())
                    .style(currentUnit == unit ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY)
                    .build()
            );
        }

        return editor.loreEmptyLine()
            .loreLine(this.clickToResetAmount)
            .createItem(session.getViewer(), Material.WHITE_STAINED_GLASS_PANE);
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var amount = this.getOrCreateAmount(session);

        if (clickType.isShiftClick()) {
            if (amount.getValue() != 1) {
                amount.setValue(1);
                RESET_SOUND.play(session.getViewer());
            }
        } else {
            amount.nextUnit();
            SoundBase.CLICK.play(session.getViewer());
        }

        return this.returningResult;
    }
}
