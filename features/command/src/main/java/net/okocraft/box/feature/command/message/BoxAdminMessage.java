package net.okocraft.box.feature.command.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.QuadArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.message.argument.TripleArgument;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.UserStockHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
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

    public static final Component VERSION_HELP =
            translatable("box.command.boxadmin.version.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.version.help.description", GRAY));

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_SENDER =
            (targetName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.give.success.sender")
                            .args(
                                    text(targetName, AQUA),
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_TARGET =
            (senderName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.give.success.target")
                            .args(
                                    text(senderName, AQUA),
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final Component GIVE_HELP =
            translatable("box.command.boxadmin.give.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.give.help.description", GRAY));

    public static final TripleArgument<String, BoxItem, Integer> SET_SUCCESS_SENDER =
            (targetName, item, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.set.success.sender")
                            .args(
                                    text(targetName, AQUA),
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final TripleArgument<String, BoxItem, Integer> SET_SUCCESS_TARGET =
            (senderName, item, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.set.success.target")
                            .args(
                                    text(senderName, AQUA),
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final Component SET_HELP =
            translatable("box.command.boxadmin.set.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.set.help.description", GRAY));

    public static final QuadArgument<String, BoxItem, Integer, Integer> TAKE_SUCCESS_SENDER =
            (targetName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.take.success.sender")
                            .args(
                                    text(targetName, AQUA),
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> TAKE_SUCCESS_TARGET =
            (senderName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.take.success.target")
                            .args(
                                    text(senderName, AQUA),
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final Component TAKE_HELP =
            translatable("box.command.boxadmin.take.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.take.help.description", GRAY));

    public static final Component REGISTER_IS_AIR = translatable("box.command.boxadmin.register.is-air", RED);

    public static final SingleArgument<ItemStack> REGISTER_ALREADY_REGISTERED =
            item ->
                    translatable()
                            .key("box.command.boxadmin.register.already-registered")
                            .args(item.displayName().color(AQUA).hoverEvent(item))
                            .color(RED)
                            .build();

    public static final SingleArgument<BoxCustomItem> REGISTER_SUCCESS =
            item ->
                    translatable()
                            .key("box.command.boxadmin.register.success")
                            .args(
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
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

    public static final Component REGISTER_HELP =
            translatable("box.command.boxadmin.register.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.register.help.description", GRAY));

    public static final Component RELOAD_START =
            translatable("box.command.boxadmin.reload.start", GRAY);

    public static final Component RELOAD_FINISH =
            translatable("box.command.boxadmin.reload.finish", GRAY);

    public static final Component RELOAD_HELP =
            translatable("box.command.boxadmin.reload.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.reload.help.description", GRAY));

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

    public static final Component RENAME_HELP =
            translatable("box.command.boxadmin.rename.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.rename.help.description", GRAY));

    public static final Component INFINITY_MODE_ENABLE = translatable("box.command.boxadmin.infinity.enabled");

    public static final Component INFINITY_MODE_DISABLED = translatable("box.command.boxadmin.infinity.disabled");

    public static final SingleArgument<Boolean> INFINITY_MODE_TOGGLE =
            enabled ->
                    translatable()
                            .key("box.command.boxadmin.infinity.toggle")
                            .args(enabled ? INFINITY_MODE_ENABLE.color(GREEN) : INFINITY_MODE_DISABLED.color(RED))
                            .color(GRAY)
                            .build();

    public static final DoubleArgument<Player, Boolean> INFINITY_MODE_TOGGLE_SENDER =
            (target, enabled) ->
                    translatable()
                            .key("box.command.boxadmin.infinity.toggle-sender")
                            .args(
                                    text(target.getName(), AQUA),
                                    enabled ? INFINITY_MODE_ENABLE.color(GREEN) : INFINITY_MODE_DISABLED.color(RED)
                            )
                            .color(GRAY)
                            .build();

    public static final DoubleArgument<CommandSender, Boolean> INFINITY_MODE_TOGGLE_TARGET =
            (sender, enabled) ->
                    translatable()
                            .key("box.command.boxadmin.infinity.toggle-target")
                            .args(
                                    text(sender.getName(), AQUA),
                                    enabled ? INFINITY_MODE_ENABLE.color(GREEN) : INFINITY_MODE_DISABLED.color(RED)
                            )
                            .color(GRAY)
                            .build();

    public static final Component INFINITY_MODE_TIP = translatable("box.command.boxadmin.infinity.tip", GRAY);

    public static final Component INFINITY_HELP =
            translatable("box.command.boxadmin.infinity.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.infinity.help.description", GRAY));

    public static final SingleArgument<UserStockHolder> RESET_SUCCESS_SENDER =
            target -> translatable()
                    .key("box.command.boxadmin.reset.success.sender")
                    .args(text(target.getName(), AQUA))
                    .color(GRAY)
                    .build();

    public static final SingleArgument<CommandSender> RESET_SUCCESS_TARGET =
            sender -> translatable()
                    .key("box.command.boxadmin.reset.success.target")
                    .args(text(sender.getName(), AQUA))
                    .color(GRAY)
                    .build();

    public static final Component RESET_CANCEL =
            translatable("box.command.boxadmin.reset.cancel", GRAY);

    public static final SingleArgument<UserStockHolder> RESET_CONFIRMATION =
            target -> text()
                    .append(
                            translatable()
                                    .key("box.command.boxadmin.reset.confirmation.info")
                                    .args(text(target.getName(), AQUA))
                                    .color(GRAY)
                                    .build()
                    ).append(newline())
                    .append(translatable("box.command.boxadmin.reset.confirmation.warning", GRAY))
                    .append(newline())
                    .append(
                            translatable()
                                    .key("box.command.boxadmin.reset.confirmation.confirm-command")
                                    .args(text("/boxadmin reset confirm", AQUA))
                                    .color(GRAY)
                                    .build()
                    ).append(newline())
                    .append(
                            translatable()
                                    .key("box.command.boxadmin.reset.confirmation.cancel-command")
                                    .args(text("/boxadmin reset cancel", AQUA))
                                    .color(GRAY)
                                    .build()
                    )
                    .build();

    public static final Component RESET_HELP =
            translatable("box.command.boxadmin.reset.help.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.command.boxadmin.reset.help.description", GRAY));

    private BoxAdminMessage() {
        throw new UnsupportedOperationException();
    }
}
