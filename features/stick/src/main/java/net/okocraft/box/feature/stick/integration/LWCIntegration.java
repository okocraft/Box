package net.okocraft.box.feature.stick.integration;

import com.griefcraft.integration.IPermissions;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import net.okocraft.box.feature.stick.function.container.ContainerOperation;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

final class LWCIntegration {

    static boolean canModify(@NotNull Player player, @NotNull BlockState state, @NotNull ContainerOperation.OperationType operationType) {
        var protection = LWC.getInstance().findProtection(state);

        if (protection == null) {
            return true;
        }

        return switch (protection.getType()) {
            case PUBLIC, PASSWORD, PRIVATE -> true; // the click to the chest has already been rejected
            case DONATION -> operationType == ContainerOperation.OperationType.WITHDRAW || canAccess(player, protection);
            case SUPPLY -> operationType == ContainerOperation.OperationType.DEPOSIT || canAccess(player, protection);
            case DISPLAY -> canAccess(player, protection);
            default -> false; // unknown protection type?
        };
    }

    private static boolean canAccess(@NotNull Player player, @NotNull Protection protection) {
        // copied from https://github.com/pop4959/LWCX/blob/master/src/main/java/com/griefcraft/listeners/LWCPlayerListener.java#L798-L815
        if (LWC.getInstance().canAdminProtection(player, protection)) {
            return true;
        }

        boolean canAccess = false;
        if (protection.getAccess(player.getUniqueId().toString(), Permission.Type.PLAYER) == Permission.Access.PLAYER) {
            canAccess = true;
        } else if (protection.getAccess(player.getName(), Permission.Type.PLAYER) == Permission.Access.PLAYER) {
            canAccess = true;
        } else {
            IPermissions permissions = LWC.getInstance().getPermissions();
            if (permissions != null) {
                for (String groupName : permissions.getGroups(player)) {
                    if (protection.getAccess(groupName, Permission.Type.GROUP) == Permission.Access.PLAYER) {
                        canAccess = true;
                    }
                }
            }
        }

        return canAccess;
    }

    private LWCIntegration() {
        throw new UnsupportedOperationException();
    }
}
