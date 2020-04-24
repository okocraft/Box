package net.okocraft.box.config;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.okocraft.box.Box;
import net.okocraft.box.util.ReflectionUtil;

public final class Messages extends CustomConfig {
    
    /**
     * コンストラクタ。
     * @deprecated 内部利用限定。このコンストラクタを使わず、{@code Box.getInstance().getAPI().getMessages()}を使用すること。
     */
    @Deprecated
    public Messages() {
        super("messages.yml");
    }

    /**
     * メッセージコンポーネントをプレイヤーに送信する。主に{@link ItemStack}のホバーを送信するために使用される。
     * 
     * @param sender メッセージを送る対象
     * @param addPrefix メッセージにプレフィックスを付加するかどうか
     * @param path メッセージを記述してあるconfigのパス
     * @param placeholders 適応されるプレースホルダーのマップ
     */
    public void sendMessageComponent(CommandSender sender, boolean addPrefix, String path, Map<String, BaseComponent> placeholders) {
        String prefix = addPrefix ? get().getString("command.general.info.plugin-prefix", "&8[&6Box&8]&r") + " "
                : "";
        TextComponent message = new TextComponent();
        String rawMessage = ChatColor.translateAlternateColorCodes('&', prefix + getMessage(path));
        while (true) {
            int placeholderIndexFirst = rawMessage.indexOf("%");
            if (placeholderIndexFirst == -1) {
                message.addExtra(rawMessage);
                break;
            }
            message.addExtra(rawMessage.substring(0, placeholderIndexFirst));
            rawMessage = rawMessage.substring(placeholderIndexFirst + 1);
            int placeholderIndexSecond = rawMessage.indexOf("%");
            String key = "%" + rawMessage.substring(0, placeholderIndexSecond + 1);
            message.addExtra(placeholders.getOrDefault(key, new TextComponent(key)));
            rawMessage = rawMessage.substring(placeholderIndexSecond + 1);
        }

        sender.spigot().sendMessage(message);
    }

    /**
     * メッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param addPrefix メッセージにプレフィックスを付加するかどうか
     * @param path メッセージを記述してあるconfigのパス
     * @param placeholders 適応されるプレースホルダーのマップ
     */
    public void sendMessage(CommandSender sender, boolean addPrefix, String path, Map<String, Object> placeholders) {
        String prefix = addPrefix ? get().getString("command.general.info.plugin-prefix", "&8[&6Box&8]&r") + " " : "";
        String message = prefix + getMessage(path);
        for (Map.Entry<String, Object> placeholder : placeholders.entrySet()) {
            message = message.replace(placeholder.getKey(), placeholder.getValue().toString());
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return;
    }

    /**
     * メッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param path メッセージを記述してあるconfigのパス
     * @param placeholders 適応されるプレースホルダーのマップ
     */
    public void sendMessage(CommandSender sender, String path, Map<String, Object> placeholders) {
        sendMessage(sender, true, path, placeholders);
    }

    /**
     * メッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param path メッセージを記述してあるconfigのパス
     */
    public void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, Map.of());
    }

    /**
     * メッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param addPrefix メッセージにプレフィックスを付加するかどうか
     * @param path メッセージを記述してあるconfigのパス
     */
    public void sendMessage(CommandSender sender, boolean addPrefix, String path) {
        sendMessage(sender, addPrefix, path, Map.of());
    }

    /**
     * configからpathを元にメッセージを取得する。存在しない場合はpathをそのまま返す。
     * 
     * @param path メッセージを記述してあるconfigのパス
     * @return 取得されたメッセージか、渡されたpath
     */
    public String getMessage(String path) {
        return Objects.requireNonNullElse(get().getString(path, path), path);
    }

    /**
     * 禁止されたワールド内でBoxの機能を使った旨のメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendDisabledWorld(CommandSender sender) {
        sendMessage(sender, "command.general.error.in-disabled-world");
    }

    /**
     * インベントリ操作を行った結果、インベントリが一杯で失敗したというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendInventoryIsFull(CommandSender sender) {
        sendMessage(sender, "command.general.error.inventory-is-full");
    }

    /**
     * コマンドの引数が足りなかったというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendNotEnoughArguments(CommandSender sender) {
        sendMessage(sender, "command.general.error.not-enough-arguments");
    }

    /**
     * アイテムを預けたりするときに、インベントリ内のアイテムが足りないというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendNotEnoughItem(CommandSender sender) {
        sendMessage(sender, "command.general.error.not-enough-item");
    }

    /**
     * コマンドの引数が不正だったというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param argument 不正だった引数
     */
    public void sendInvalidArgument(CommandSender sender, String argument) {
        sendMessage(sender, "command.general.error.invalid-argument", Map.of("%argument%", argument));
    }

    /**
     * autostore設定が変更された旨のメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param switchTo Autostoreを有効化するかどうかのプレースホルダー
     */
    public void sendAutoStore(CommandSender sender, ItemStack item, boolean switchTo) {
        sendMessageComponent(sender, true, "command.box.auto-store.info.changed",
                Map.of("%item%", toTextComponent(item), "%is-enabled%", new TextComponent(String.valueOf(switchTo))));
    }

    /**
     * すべてのアイテムのautostore設定が変更された旨のメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param switchTo Autostoreを有効化するかどうかのプレースホルダー
     */
    public void sendAutoStoreAll(CommandSender sender, boolean switchTo) {
        sendMessage(sender, "command.box.auto-store.info.changed-all",
                Map.of("%is-enabled%", String.valueOf(switchTo)));
    }

    /**
     * 原因不明のエラーが発生したというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendUnknownError(CommandSender sender) {
        sendMessage(sender, "command.general.error.unknown-exception");
    }

    /**
     * アイテムがデータベースに存在しないというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendItemNotFound(CommandSender sender) {
        sendMessage(sender, "command.general.error.item-not-found");
    }

    /**
     * autostore設定リストのヘッダーを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param player autostore設定を調べるプレイヤー
     * @param page 表示するページ
     * @param currentLine 表示したページの一番最後の行の番号
     * @param maxLine すべての行数
     */
    public void sendAutoStoreListHeader(CommandSender sender, String player, int page, int currentLine, int maxLine) {
        sendMessage(sender, "command.box.auto-store-list.info.header", Map.of(
                "%player%", player,
                "%page%", String.valueOf(page),
                "%current-line%", String.valueOf(currentLine),
                "%max-line%", String.valueOf(maxLine))
        );
    }

    /**
     * autostore設定リストの行を送信する。
     * 
     * @param sender メッセージを送る対象
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param isEnabled autostore設定
     */
    public void sendAutoStoreListFormat(CommandSender sender, ItemStack item, boolean isEnabled) {
        sendMessageComponent(sender, false, "command.box.auto-store-list.info.format", Map.of(
            "%item%", toTextComponent(item),
            "%is-enabled%", new TextComponent(String.valueOf(isEnabled))
        ));
    }

    /**
     * {@code given} に {@code sender} が {@code item} を与えたというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param given 与えられたプレイヤーの名前
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 与える量
     * @param newAmount 与えた後のストック
     */
    public void sendGiveInfoToSender(CommandSender sender, String given, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(sender, true, "command.box.give.info.sender", Map.of(
            "%player%", new TextComponent(given),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    /**
     * {@code target} に {@code giver} がアイテムを与えたというメッセージを送信する。
     * 
     * @param target アイテムを与えられたプレイヤー
     * @param giver アイテムを与えたプレイヤー
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 与えられた量
     * @param newAmount 与えられた後のストック
     */
    public void sendGiveInfoToTarget(CommandSender target, String giver, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(target, true, "command.box.give.info.player", Map.of(
            "%sender%", new TextComponent(giver),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    /**
     * {@code given} に {@code sender} が {@code item} を与えたというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param given 与えられたプレイヤーの名前
     * @param item プレースホルダーに使われる{@link ItemStack}。与えれたアイテム
     * @param amount 与えた量
     * @param newAmount 与えられた後のストック
     */
    public void sendAdminGiveInfoToSender(CommandSender sender, String given, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(sender, true, "command.boxadmin.give.info.sender", Map.of(
            "%player%", new TextComponent(given),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    /**
     * {@code target} に {@code giver} がアイテムを与えたというメッセージを送信する。
     * 
     * @param target アイテムを与えられたプレイヤー
     * @param giver 与えたプレイヤーの名前
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 与えられた量
     * @param newAmount 与えられた後のストック
     */
    public void sendAdminGiveInfoToTarget(CommandSender target, String giver, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(target, true, "command.boxadmin.give.info.player", Map.of(
            "%sender%", new TextComponent(giver),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    /**
     * ストックが足りなかったというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendNotEnoughStock(CommandSender sender) {
        sendMessage(sender, "command.general.error.not-enough-stock");
    }

    /**
     * 自分にアイテムを与えようとして失敗したというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendCannotGiveMyself(CommandSender sender) {
        sendMessage(sender, "command.box.give.error.cannot-give-myself");
    }

    /**
     * プレイヤー限定のコマンドをコンソールが送信したというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendPlayerOnly(CommandSender sender) {
        sendPlayerOnly(sender);
    }

    /**
     * 指定されたプレイヤーが見当たらなかったというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendPlayerNotFound(CommandSender sender) {
        sendMessage(sender, "command.general.error.player-not-found");
    }

    /**
     * ヘルプコマンドで表示されるヘッダを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendHelpHeader(CommandSender sender) {
        sendMessage(sender, "command.box.help.info.header");
    }

    /**
     * ヘルプコマンドで表示される一行を送信する
     * 
     * @param sender メッセージを送る対象
     * @param command ヘルプを表示するコマンド
     * @param description ヘルプを表示するコマンドの説明
     */
    public void sendHelpFormat(CommandSender sender, String command, String description) {
        sendMessage(sender, false, "command.box.help.info.format", Map.of("%command%", command, "%description%", description));
    }

    /**
     * バージョンを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param version バージョン
     */
    public void sendVersion(CommandSender sender, String version) {
        sendMessage(sender, "command.box.version.info.format", Map.of("%version%", version));
    }

    public void sendReloadSuccess(CommandSender sender) {
        sendMessage(sender, "command.boxadmin.reload.info.success");
    }

    /**
     * {@code target} の {@code item} のストックを変更したというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param target ストックを変更する対象の名前
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 変更後のストック
     */
    public void sendSetInfoToSender(CommandSender sender, String target, ItemStack item, long amount) {
        sendMessageComponent(sender, true, "command.boxadmin.set.info.sender", Map.of(
            "%player%", new TextComponent(target),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount))
        ));
    }

    /**
     * {@code sender} に {@code item} のストックを変更されたというメッセージを送信する。
     * 
     * @param target ストックを変更する対象
     * @param sender メッセージを送る対象の名前
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 変更後のストック
     */
    public void sendSetInfoToTarget(CommandSender target, String sender, ItemStack item, long amount) {
        sendMessageComponent(target, true, "command.boxadmin.set.info.player", Map.of(
            "%sender%", new TextComponent(sender),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount))
        ));
    }

    /**
     * {@code target} の {@code item} のストックを減らしたというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param target アイテムを減らす対象の名前
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 減らす量
     * @param newAmount 減らした後の量
     */
    public void sendTakeInfoToSender(CommandSender sender, String target, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(sender, true, "command.boxadmin.take.info.sender", Map.of(
            "%player%", new TextComponent(target),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    /**
     * {@code sender} に {@code item} のストックを減らされたというメッセージを送信する。
     * 
     * @param target アイテムを減らす大量
     * @param sender メッセージを送る対象の名前
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 減らした量
     * @param newAmount 減らした後の量
     */
    public void sendTakeInfoToTarget(CommandSender target, String sender, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(target, true, "command.boxadmin.give.info.player", Map.of(
            "%sender%", new TextComponent(sender),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    /**
     * アイテム情報を含むホバーを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param item プレースホルダーに使われる{@link ItemStack}
     */
    public void sendItemInfo(CommandSender sender, ItemStack item) {
        sendMessageComponent(sender, true, "command.box.iteminfo.info.format",
                Map.of("%item%", toTextComponent(item)));
    }

    /**
     * 権限がないというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param permission 足りなかった権限
     */
    public void sendNoPermission(CommandSender sender, String permission) {
        sendMessage(sender, "command.general.error.no-permission", Map.of("%permission%", permission));
    }

    /**
     * コマンドが成功しなかった時に、コマンドの使い方を送信する。
     * 
     * @param sender メッセージを送る対象
     * @param usage 使い方
     */
    public void sendUsage(CommandSender sender, String usage) {
        sendMessage(sender, "command.general.info.usage", Map.of("%usage%", usage));
    }

    /**
     * アイテムのcustomnameが変更されたというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param oldName 変更前の名前
     * @param newName 変更後の名前
     */
    public void sendItemNameChanged(CommandSender sender, String oldName, String newName) {
        sendMessage(sender, "command.boxadmin.customname.info.success", Map.of("%old%", oldName, "%new%", newName));
    }

    /**
     * アイテムを手に持っていないので、持ってくださいというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendHoldItem(CommandSender sender) {
        sendMessage(sender, "command.general.error.hold-item");
    }

    /**
     * アイテムを手に持つか、アイテムをコマンドで指定してくださいというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendHoldItemOrSpecifyItem(CommandSender sender) {
        sendMessage(sender, "command.general.error.hold-item-or-specify-item");
    }

    /**
     * アイテムがデータベースに登録されたというメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param item プレースホルダーに使われる{@link ItemStack}。登録したアイテム
     */
    public void sendItemRegistered(CommandSender sender, ItemStack item) {
        sendMessageComponent(sender, true, "command.boxadmin.register.info.success", Map.of("%item%", toTextComponent(item)));
    }

    /**
     * アイテムをコマンドで引き出した時にメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 引き出したアイテム量
     * @param stock 引き出し後のアイテム量
     */
    public void sendWithdrawItem(CommandSender sender, ItemStack item, int amount, int stock) {
        sendMessageComponent(sender, true, "command.box.withdraw.info.success", Map.of("%item%", toTextComponent(item), "%amount%", new TextComponent(String.valueOf(amount)), "%stock%", new TextComponent(String.valueOf(stock))));
    }

    /**
     * コマンドでアイテムを預けた時にメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     * @param item プレースホルダーに使われる{@link ItemStack}
     * @param amount 預けたアイテム量
     * @param stock 預けた後のアイテム量
     */
    public void sendDepositItem(CommandSender sender, ItemStack item, int amount, int stock) {
        sendMessageComponent(sender, true, "command.box.deposit.info.success", Map.of("%item%", toTextComponent(item), "%amount%", new TextComponent(String.valueOf(amount)), "%stock%", new TextComponent(String.valueOf(stock))));
    }

    /**
     * 全てのアイテムをコマンドで預けた時にメッセージを送信する。
     * 
     * @param sender メッセージを送る対象
     */
    public void sendDepositItemAll(CommandSender sender) {
        sendMessage(sender, "command.box.deposit.info.all-success");
    }

    /**
     * Converts an {@link org.bukkit.inventory.ItemStack} to a Json string for
     * sending with {@link net.md_5.bungee.api.chat.BaseComponent}'s.
     *
     * @param itemStack the item to convert
     * @return the hover evnet that show the item
     */
    @Nullable
    private TextComponent toTextComponent(ItemStack itemStack) {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for
        // serialization
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json
        // string
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        // This will just be an empty NBTTagCompound instance to invoke the saveNms
        // method
        Object nmsNbtTagCompoundObj;

        // This is the net.minecraft.server.ItemStack object received from the asNMSCopy
        // method
        Object nmsItemStackObj;

        // This is the net.minecraft.server.ItemStack after being put through
        // saveNmsItem method
        Object itemAsJsonObject;

        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.getDeclaredConstructor().newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            Box.getInstance().getLogger().log(Level.SEVERE, "failed to serialize itemstack to nms item", t);
            return null;
        }
        
        BaseComponent[] hoverEventComponents = new BaseComponent[]{
            new TextComponent(itemAsJsonObject.toString())
        };

        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
        TextComponent text = new TextComponent(Box.getInstance().getAPI().getItemData().getName(itemStack));
        text.setHoverEvent(hover);
        return text;
    }
}