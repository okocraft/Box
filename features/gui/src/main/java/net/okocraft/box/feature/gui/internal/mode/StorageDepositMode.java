package net.okocraft.box.feature.gui.internal.mode;

import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.gui.api.mode.GuiType;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class StorageDepositMode extends StorageMode {

    @Override
    public @NotNull String getName() {
        return "storage_deposit";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.STORAGE_DEPOSIT_MODE_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        processDeposit(context);
    }

    @Override
    public Set<GuiType> getApplicableGuiTypes() {
        return Set.of(GuiType.BE);
    }

    @Override
    protected @NotNull @Unmodifiable List<Component> createLore(@NotNull BoxItem item, @NotNull Player player) {
        var session = PlayerSession.get(player);

        int currentStock = session.getStockHolder().getAmount(item);
        int transactionAmount = session.getCustomNumberHolder(TRANSACTION_AMOUNT_NAME).getAmount();

        return List.of(
                Displays.STORAGE_DEPOSIT_MODE_CLICK_TO_DEPOSIT.apply(transactionAmount),
                Component.empty(),
                Displays.STORAGE_MODE_CURRENT_STOCK.apply(currentStock)
        );
    }
}
