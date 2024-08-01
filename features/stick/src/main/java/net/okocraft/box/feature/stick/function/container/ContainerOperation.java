package net.okocraft.box.feature.stick.function.container;

import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ContainerOperation<I extends Inventory>(@NotNull ContainerOperation.Context<I> context,
                                                      @NotNull Operator<I> operator,
                                                      @NotNull String permissionSuffix) {

    @Contract("_, _, _, _ -> new")
    public static <I extends Inventory> @NotNull Context<I> createContext(@NotNull BoxPlayer viewer, @NotNull ContainerOperation.OperationType operationType, @NotNull I inventory, @NotNull Location blockLocation) {
        return new Context<>(viewer, operationType, inventory, blockLocation);
    }

    public boolean run() {
        return this.operator.process(this.context);
    }

    public interface Operator<I extends Inventory> {
        boolean process(@NotNull ContainerOperation.Context<I> context);
    }

    public enum OperationType {
        DEPOSIT,
        WITHDRAW
    }

    public record Context<I extends Inventory>(@NotNull BoxPlayer player,
                                               @NotNull ContainerOperation.OperationType operationType,
                                               @NotNull I inventory,
                                               @NotNull Location blockLocation) {
    }
}
