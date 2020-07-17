/*
 * Box
 * Copyright (C) 2019 OKOCRAFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.okocraft.box.command.box;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.okocraft.box.command.Commands;
import net.okocraft.box.gui.CategorySelectorGUI;

/**
 * /boxコマンドの実装クラス。シングルトンであり、外部でnewするとIllegalArgumentExceptionをスローする。
 */
public class BoxCommand extends Commands {

    private static BoxCommand instance;

    /**
     * コンストラクタ
     */
    public BoxCommand() {
        super("box");
        if (instance != null) {
            throw new IllegalStateException("Command " + instance + " is already instantiated.");
        }

        instance = this;

        register(new AutoStoreCommand());
        register(new AutoStoreListCommand());
        register(new GiveCommand());
        register(new ItemInfoCommand());
        register(new HelpCommand());
        register(new StickCommand());
        register(new VersionCommand());
        register(new WithdrawCommand());
        register(new DepositCommand());
    }

    /**
     * @return the instance
     */
    static BoxCommand getInstance() {
        return instance;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && args.length == 0) {
            config.playGUIOpenSound((Player) sender);
            ((Player) sender).openInventory(new CategorySelectorGUI().getInventory());
            return false;
        }

        return super.onCommand(sender, command, label, args);
    }
}