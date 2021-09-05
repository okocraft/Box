package net.okocraft.box.command.message;

import net.kyori.adventure.text.Component;
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
                            .key("box.command.version.info")
                            .args(text(version, AQUA))
                            .color(GRAY)
                            .build();

    public static final SingleArgument<String> GIVE_PLAYER_NOT_FOUND =
            playerName ->
                    translatable()
                            .key("box.command.boxadmin.give.player-not-found")
                            .args(text(playerName, AQUA))
                            .color(RED)
                            .build();

    public static final SingleArgument<String> GIVE_ITEM_NOT_FOUND =
            itemName ->
                    translatable()
                            .key("box.command.boxadmin.give.item-not-found")
                            .args(text(itemName, AQUA))
                            .color(RED)
                            .build();

    public static final SingleArgument<String> GIVE_INVALID_NUMBER =
            invalid ->
                    translatable()
                            .key("box.command.boxadmin.give.invalid-number")
                            .args(text(invalid, AQUA))
                            .color(RED)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_SENDER =
            (targetName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.give.success-sender")
                            .args(
                                    text(targetName, AQUA), item.getDisplayName(),
                                    text(increments, AQUA), text(currentAmount, AQUA)
                            )
                            .color(GRAY)
                            .build();

    public static final QuadArgument<String, BoxItem, Integer, Integer> GIVE_SUCCESS_TARGET =
            (senderName, item, increments, currentAmount) ->
                    translatable()
                            .key("box.command.boxadmin.give.success-target")
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

    private BoxAdminMessage() {
        throw new UnsupportedOperationException();
    }
}
