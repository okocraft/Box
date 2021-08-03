package net.okocraft.box.command;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import net.okocraft.box.Box;
import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Config;
import net.okocraft.box.config.Messages;
import net.okocraft.box.database.ItemData;
import net.okocraft.box.database.PlayerData;

/**
 * サブコマンドに必要な各種情報を保持するクラス。使用するには具体的な実装が必要。
 */
public abstract class BaseCommand {

    /**
     * プラグインのインスタンス
     */
    protected final Box plugin = Box.getInstance();
    
    /**
     * config.ymlの設定
     */
    protected final Config config = plugin.getAPI().getConfig();

    /**
     * messages.ymlの設定
     */
    protected final Messages messages = plugin.getAPI().getMessages();

    /**
     * categories.ymlの設定
     */
    protected final Categories categories = plugin.getAPI().getCategories();

    /**
     * playerデータのアクセサ
     */
    protected final PlayerData playerData = plugin.getAPI().getPlayerData();

    /**
     * アイテムデータのアクセサ
     */
    protected final ItemData itemData = plugin.getAPI().getItemData();

    private final String name;
    private final String permissionNode;
    private final int leastArgLength;
    private final boolean isPlayerOnly;
    private final String usage;
    private final List<String> alias;

    /**
     * コンストラクタ
     * 
     * @param name コマンド名
     * @param permissionNode コマンドの権限
     * @param leastArgLength 最低限必要な引数の数
     * @param isPlayerOnly コンソールからも利用できるかどうか
     * @param usage コマンドの使用法
     * @param alias コマンドの略称
     */
    protected BaseCommand(String name, String permissionNode, int leastArgLength, boolean isPlayerOnly, String usage, String ... alias) {
        this.name = name;
        this.permissionNode = permissionNode;
        this.leastArgLength = leastArgLength;
        this.isPlayerOnly = isPlayerOnly;
        this.usage = usage;
        this.alias = Arrays.asList(alias);
    }

    /**
     * 各コマンドの処理
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return コマンドが成功したらtrue
     */
    public abstract boolean runCommand(CommandSender sender, String[] args);

    /**
     * 各コマンドのタブ補完の処理
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return その時のタブ補完のリスト
     */
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    /**
     * コマンドの名前を取得する。
     *
     * @return コマンドの名前
     */
    public String getName() {
        return name;
    }

    /**
     * このコマンドの権限を取得する。
     *
     * @return 権限
     */
    public String getPermissionNode() {
        return permissionNode;
    }

    /**
     * プレイヤーのみが使用可能なコマンドかどうかを取得する
     * 
     * @return プレイヤーのみ使えるコマンドならtrue、さもなくばfalse
     */
    public boolean isPlayerOnly() {
        return isPlayerOnly;
    }

    /**
     * 最低限必要な引数の長さを取得する。
     *
     * @return 最低限の引数の長さ
     */
    public int getLeastArgLength() {
        return leastArgLength;
    }

    /**
     * 渡した長さがこのコマンドに必要な最低限の長さに届いているかどうか調べる。
     * 
     * @param argsLength 調べる引数の長さ
     * @return このコマンドを実行できる引数の長さがあるならtrue、さもなくばfalse
     */
    public boolean isValidArgsLength(int argsLength) {
        return getLeastArgLength() <= argsLength;
    }

    /**
     * コマンドの引数の内容を取得する。例: "/box autostoreList [page]"
     *
     * @return 引数の内容
     */
    public String getUsage() {
        return usage;
    }

    /**
     * コマンドの略称のリストを取得する。
     * 
     * @return 略称のリスト
     */
    public List<String> getAlias() {
        return alias;
    }

    /**
     * コマンドの説明を取得する。例: "アイテムの自動収納の設定をリストにして表示する。"
     *
     * @return コマンドの説明
     */
    public String getDescription() {
        return messages.getMessage("command.command-description." + getName());
    }

    /**
     * このコマンドを使う権限があるか調べる。
     * 
     * @param sender コマンド送信者
     * @return 権限があればtrue なければfalse
     * @see CommandSender#hasPermission(String)
     */
    public boolean hasPermission(CommandSender sender) {
        if (permissionNode == null || permissionNode.isEmpty()) {
            return true;
        }

        return sender.hasPermission(getPermissionNode());
    }

    /**
     * numberを解析してint型にして返す。numberのフォーマットがintではないときはdefを返す。
     *
     * @param number 解析する文字列
     * @param def    解析に失敗したときに返す数字
     * @return int型の数字。
     * @author LazyGon
     * @since v1.1.0
     */
    protected int parseIntOrDefault(String number, int def) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException exception) {
            return def;
        }
    }
}