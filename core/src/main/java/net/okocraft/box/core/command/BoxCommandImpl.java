package net.okocraft.box.core.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.command.base.BoxCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class BoxCommandImpl extends BaseCommand implements BoxCommand {

    @Override
    public @NotNull String getName() {
        return "box";
    }

    @Override
    public @NotNull String getPermissionNode() {
        return "box.command";
    }

    @Override
    public @NotNull @Unmodifiable Set<String> getAliases() {
        return Set.of("b", "okobox");
    }

    @Override
    public @NotNull Component getHelp() {
        return text().append(translatable("box.command.box.help.command-line", AQUA))
                .append(text(" - ", DARK_GRAY))
                .append(translatable("box.command.box.help.description", GRAY))
                .build();
    }
}
