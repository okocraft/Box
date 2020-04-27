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

package net.okocraft.box.command.boxadmin;

import net.okocraft.box.command.Commands;

/**
 * /boxadminコマンドの実装クラス。シングルトンであり、外部でnewするとIllegalArgumentExceptionをスローする。
 */
public class BoxAdminCommand extends Commands {

    private static BoxAdminCommand instance;

    /**
     * コンストラクタ
     */
    public BoxAdminCommand() {
        super("boxadmin");
        if (instance != null) {
            throw new IllegalStateException("Command " + instance + " is already instantiated.");
        }

        instance = this;

        register(new AddCategoryCommand());
        register(new AutoStoreCommand());
        register(new AutoStoreListCommand());
        register(new GiveCommand());
        register(new HelpCommand());
        register(new ReloadCommand());
        register(new SetCommand());
        register(new TakeCommand());
        register(new CustomNameCommand());
        register(new RegisterCommand());
        
        register(new MigrateCommand());
    }

    /**
     * @return the instance
     */
    static BoxAdminCommand getInstance() {
        return instance;
    }
}