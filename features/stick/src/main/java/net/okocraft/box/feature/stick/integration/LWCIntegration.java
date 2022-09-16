package net.okocraft.box.feature.stick.integration;

import com.griefcraft.integration.IPermissions;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LWCIntegration {

    public static boolean canModifyInventory(@NotNull Player player, @NotNull BlockState state, boolean deposit) {
        if (Bukkit.getPluginManager().getPlugin("LWC") == null) {
            return true;
        }

        var protection = LWC.getInstance().findProtection(state);

        if (protection == null) {
            return true;
        }

        return switch (protection.getType().name()) { // DISPLAY is not present in 2.1.5.
            case "DONATION" -> !deposit || canAccess(player, protection);
            case "DISPLAY" -> canAccess(player, protection);
            default -> true; // Otherwise, the click to the chest has already been rejected.
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
}
