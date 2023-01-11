package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public record ContainerOperation<I extends Inventory>(@NotNull BoxPlayer boxPlayer, @NotNull String permissionSuffix,
                                                      @NotNull OperationType type,
                                                      @NotNull I inventory, @NotNull Operator<I> operator,
                                                      @NotNull Location clickedBlockLocation) {

    public boolean run() {
        return operator.process(boxPlayer, type, inventory, clickedBlockLocation);
    }

    public interface Operator<I extends Inventory> {
        boolean process(@NotNull BoxPlayer boxPlayer, @NotNull OperationType type, @NotNull I inventory, @NotNull Location clickedLocation);
    }

    public enum OperationType {
        DEPOSIT,
        WITHDRAW
    }
}
