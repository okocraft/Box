package net.okocraft.box.feature.gui.api.buttons.amount;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.lang.Styles;
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

public class UnitChangeButton extends AmountModificationButton {

    private static final SoundBase RESET_SOUND = SoundBase.builder().sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).pitch(1.5f).build();

    private final Component displayName;
    private final Component clickToResetAmount;
    private final ClickResult returningResult;

    public UnitChangeButton(int slot, @NotNull TypedKey<Amount> dataKey,
                            @NotNull Component displayName, @NotNull Component clickToResetAmount,
                            @NotNull ClickResult returningResult) {
        super(slot, dataKey);

        this.displayName = displayName;
        this.clickToResetAmount = clickToResetAmount;
        this.returningResult = returningResult;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        icon.editMeta(meta -> {
            var viewer = session.getViewer();
            meta.displayName(TranslationUtil.render(displayName, viewer));

            var lore = new ArrayList<Component>();

            var currentUnit = getOrCreateAmount(session).getUnit();

            for (var unit : Amount.Unit.values()) {
                lore.add(
                        Component.text()
                                .content(" > " + unit.getAmount())
                                .style(currentUnit == unit ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY)
                                .build()
                );
            }

            lore.add(Component.empty());
            lore.add(TranslationUtil.render(clickToResetAmount, viewer));

            meta.lore(lore);
        });

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        var amount = getOrCreateAmount(session);

        if (clickType.isShiftClick()) {
            if (amount.getValue() != 1) {
                amount.setValue(1);
                RESET_SOUND.play(session.getViewer());
            }
        } else {
            amount.nextUnit();
            SoundBase.CLICK.play(session.getViewer());
        }

        return returningResult;
    }
}
