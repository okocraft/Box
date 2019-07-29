package net.okocraft.box.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.okocraft.box.Box;
import net.okocraft.box.database.Database;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.MessageConfig;

public interface BoxCommand {

    static final Box INSTANCE = Box.getInstance();
    static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();
    static final MessageConfig MESSAGE_CONFIG = INSTANCE.getMessageConfig();
    static final Database DATABASE = INSTANCE.getDatabase();

    /**
     * コマンドの処理内容
     * 
     * @param sender
     * @param args
     * @return コマンドが成功したらtrue
     */
    boolean runCommand(CommandSender sender, String[] args);

    /**
     * コマンドのタブ補完の内容
     * 
     * @param sender
     * @param args
     * @return その時のタブ補完のリスト
     */
    List<String> runTabComplete(CommandSender sender, String[] args);

    /**
     * コマンドの名前を取得する。
     * 
     * @return コマンドの名前
     */
    String getCommandName();

    /**
     * このコマンドの権限を取得する。
     * 
     * @return 権限
     */
    String getPermissionNode();

    /**
     * 最低限必要な引数の長さを取得する。
     * 
     * @return 最低限の引数の長さ
     */
    int getLeastArgLength();

    /**
     * コマンドの引数の内容を取得する。例: "/box autostoreList [page]"
     * 
     * @return 引数の内容
     */
    String getUsage();

    /**
     * コマンドの説明を取得する。例: "アイテムの自動収納の設定をリストにして表示する。"
     * 
     * @return コマンドの説明
     */
    String getDescription();
}