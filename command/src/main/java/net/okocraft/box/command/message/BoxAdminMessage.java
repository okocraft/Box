package net.okocraft.box.command.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.okocraft.box.api.message.argument.QuadArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class BoxAdminMessage {

    public static final SingleArgument<String> VERSION_INFO =
            version ->
                    translatable()
                            .key("box.command.boxadmin.version.info")
                            .args(text(version, AQUA))
                            .color(GRAY)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_SENDER =
            (targetName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.give.success.sender")
                            .args(
                                    text(targetName, AQUA), item.getDisplayName(),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_TARGET =
            (senderName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.give.success.target")
                            .args(
                                    text(senderName, AQUA), item.getDisplayName(),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final Component REGISTER_IS_AIR = translatable("box.command.boxadmin.register.is-air", RED);

    public static final SingleArgument<ItemStack> REGISTER_ALREADY_REGISTERED =
            item ->
                    translatable()
                            .key("box.command.boxadmin.register.already-registered")
                            .args(item.displayName().hoverEvent(item))
                            .color(RED)
                            .build();

    public static final SingleArgument<BoxCustomItem> REGISTER_SUCCESS =
            item ->
                    translatable()
                            .key("box.command.boxadmin.register.success")
                            .args(
                                    item.getDisplayName().hoverEvent(item.getOriginal()),
                                    text(item.getPlainName(), AQUA)
                                            .clickEvent(ClickEvent.copyToClipboard(item.getPlainName()))
                            )
                            .color(GRAY)
                            .build();

    public static final Component REGISTER_TIP_RENAME = translatable("box.command.boxadmin.register.tip-rename", GRAY);

    public static final SingleArgument<Throwable> REGISTER_FAILURE =
            throwable ->
                    translatable()
                            .key("box.command.boxadmin.register.failure")
                            .args(text(throwable.getMessage(), WHITE))
                            .color(RED)
                            .build();

    public static final SingleArgument<BoxItem> RENAME_IS_NOT_CUSTOM_ITEM =
            item ->
                    translatable()
                            .key("box.command.boxadmin.rename.is-not-custom-item")
                            .args(text(item.getPlainName(), AQUA).hoverEvent(item.getOriginal()))
                            .color(RED)
                            .build();

    public static final SingleArgument<String> RENAME_ALREADY_USED_NAME =
            name ->
                    translatable()
                            .key("box.command.boxadmin.rename.already-used-name")
                            .args(text(name, AQUA))
                            .color(RED)
                            .build();

    public static final SingleArgument<BoxItem> RENAME_SUCCESS =
            item ->
                    translatable()
                            .key("box.command.boxadmin.rename.success")
                            .args(text(item.getPlainName(), AQUA).hoverEvent(item.getOriginal()))
                            .color(GRAY)
                            .build();

    public static final SingleArgument<Throwable> RENAME_FAILURE =
            throwable ->
                    translatable()
                            .key("box.command.boxadmin.rename.failure")
                            .args(text(throwable.getMessage(), WHITE))
                            .color(RED)
                            .build();

    private BoxAdminMessage() {
        throw new UnsupportedOperationException();
    }
}
