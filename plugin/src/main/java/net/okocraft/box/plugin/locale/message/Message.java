package net.okocraft.box.plugin.locale.message;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum Message {

    // プレースホルダーは {} で設定する。デフォルトのメッセージは、左から 0, 1, ... とナンバリングされる。
    PREFIX("prefix", "&8[&6Box&8]&r "),
    VERSION("version", "&eVersion {}"),

    ERROR_DISABLED_WORLD("error.disabled-world", "&cこのワールドで Box は使用できません。"),
    ERROR_INVENTORY_FULL("error.inventory-full", "&cインベントリに空きがありません。"),
    ERROR_NOT_ENOUGH_ARGS("error.not-enough-args", "&cコマンドの引数が足りません。"),
    ERROR_NOT_ENOUGH_ITEM("error.not-enough-item", "&cアイテムが足りません。"),
    ERROR_NOT_ENOUGH_MONEY("error.not-enough-money", "&c所持金が足りません。"),
    ERROR_PLAYER_NOT_FOUND("error.player-not-found", "&cプレイヤー &b{}&c は見つかりませんでした。"),
    ERROR_ITEM_NOT_FOUND("error.item-not-found", "&cアイテム &b{}&c は存在しません。"),
    ERROR_CATEGORY_NOT_FOUND("error.category-not-found", "&cカテゴリー &b{}&c は存在しません。"),
    ERROR_COMMAND_NOT_FOUND("error.command-not-found", "&cそのコマンドは存在しません。"),
    ERROR_INVALID_ARGS("error.invalid-args", "&c引数 &b{}&c は存在しません。"),
    ERROR_INVALID_NUMBER("error.invalid-num", "&c正の整数を指定してください。"),
    ERROR_ONLY_PLAYER("error.only-player", "&cこのコマンドはゲーム内のプレイヤーのみ使用できます。"),
    ERROR_NO_PERMISSION("error.no-permission", "&c権限 &b{}&c がありません。"),

    AVAILABLE_COMMANDS_VIEW("command.available", "&7利用可能なコマンドは &b/{} help&7 で確認できます。"),

    DEPOSIT_NO_ITEM_IN_MAIN("deposit.no-item-in-main-hand", "&c手に何も持っていません。何か持つか、アイテムを指定してください。"),
    DEPOSIT_ITEM("deposit.item", "&b{}&7 を &b{}個&7 預けました (現在: &b{}&7個)"),
    DEPOSIT_ALL("deposit.all-items", "&7インベントリ内のすべてのアイテムを預けました: {}"),

    TRANSACTION_DETAIL_HOVER_TEXT("transaction.detail.hover-text", "&8[&b詳細&8]"),
    TRANSACTION_DETAIL_INCREASED("transaction.detail.increased", "&a+ &f{}x{}"),
    TRANSACTION_DETAIL_DECREASED("transaction.detail.decreased", "&c- &f{}x{}"),

    USAGE_COMMAND("usage.command.usage", "&b/{} <args> &7(略: {})"),

    USAGE_COMMAND_BOX_DEPOSIT("usage.command.box.deposit", "&8> &b/{} {} [<amount> | <ITEM> [amount] | all]")
    ;

    private final static Cache<Message, String> DEFAULT_MESSAGE =
            CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    private final String path;
    private final String def;

    Message(@NotNull String path, @NotNull String def) {
        this.path = path;
        this.def = def;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String getDefault() {
        try {
            return DEFAULT_MESSAGE.get(this, this::setPlaceholderNumber);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return setPlaceholderNumber();
        }
    }

    @NotNull
    private String setPlaceholderNumber() {
        StringBuilder builder = new StringBuilder();
        char[] b = def.toCharArray();
        int count = 0;

        for (int i = 0; i < b.length; i++) {
            if (b[i] == '{' && i + 1 < b.length && b[i + 1] == '}') {
                builder.append('{').append(count).append('}');
                count++;
                i++;
            } else {
                builder.append(b[i]);
            }
        }

        return builder.toString();
    }

    public static void clearCache() {
        DEFAULT_MESSAGE.invalidateAll();
    }
}
