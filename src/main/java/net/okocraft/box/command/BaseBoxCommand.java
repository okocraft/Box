package net.okocraft.box.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.okocraft.box.util.OtherUtil;

public abstract class BaseBoxCommand implements BoxCommand {

    /**
     * コンストラクタ
     */
    protected BaseBoxCommand() {
        OtherUtil.registerPermission(getPermissionNode(), "box.*");
    }

    /**
     * 権限や引数の長さなどが基準を満たしているか確認する。
     * 
     * @return 満たしていればtrue
     */
    protected boolean validate(CommandSender sender, String[] args) {
        if ((sender instanceof Player) && !sender.hasPermission(getPermissionNode())) {
            sender.sendMessage(MESSAGE_CONFIG.getPermissionDenied());
            return false;
        }

        if (args.length < getLeastArgLength()) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughArguments());
            return false;
        }

        return true;
    }
}