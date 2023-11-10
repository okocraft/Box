package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.version.BuildData;
import net.okocraft.box.api.model.version.VersionInfo;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class VersionCommand extends AbstractCommand {

    private static final Component INDENT = Component.text("  ");
    private static final Component COLON = Components.grayText(": ");

    public VersionCommand() {
        super("version", "box.admin.command.version", Set.of("v", "ver"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var versionInfo = BoxProvider.get().getVersionInfo();

        Component text;

        if (args.length < 2 || !args[1].equalsIgnoreCase("details")) {
            text = BoxAdminMessage.VERSION_INFO.apply(versionInfo.version());
        } else {
            text = createDetails(versionInfo);
        }

        sender.sendMessage(text);
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.VERSION_HELP;
    }

    private @NotNull Component createDetails(@NotNull VersionInfo versionInfo) {
        return Component.text()
                .append(BoxAdminMessage.VERSION_INFO_DETAILS_TITLE)
                .append(Component.newline())
                .append(INDENT).append(BoxAdminMessage.VERSION_INFO_DETAILS_VERSION)
                .append(COLON).append(Components.aquaText(versionInfo.version()))
                .append(Components.grayText(" (")).append(getVersionType(versionInfo)).append(Components.grayText(")"))
                .append(Component.newline())
                .append(INDENT).append(BoxAdminMessage.VERSION_INFO_DETAILS_COMMIT_HASH)
                .append(COLON).append(Components.aquaText(versionInfo.buildData().commitHash()))
                .append(Component.newline())
                .append(formatBuildData(versionInfo.buildData()))
                .build();
    }

    private @NotNull Component getVersionType(@NotNull VersionInfo versionInfo) {
        return versionInfo.isReleaseVersion() ?
                BoxAdminMessage.VERSION_INFO_DETAILS_RELEASE_VERSION :
                BoxAdminMessage.VERSION_INFO_DETAILS_DEVELOPMENT_VERSION;
    }

    private @NotNull Component formatBuildData(@NotNull BuildData buildData) {
        var text = Component.text();

        if (!buildData.buildDate().equals(Instant.MIN)) {
            text.append(INDENT).append(BoxAdminMessage.VERSION_INFO_DETAILS_BUILD_DATE)
                    .append(COLON).append(formatBuildDate(buildData.buildDate()))
                    .append(Component.newline());
        }

        text.append(INDENT).append(BoxAdminMessage.VERSION_INFO_DETAILS_BUILD_AT).append(COLON);

        if (buildData instanceof BuildData.Local) {
            text.append(BoxAdminMessage.VERSION_INFO_DETAILS_LOCAL);
        } else if (buildData instanceof BuildData.CI ci) {
            var display = Component.text().content(ci + " #" + ci.buildNumber()).color(NamedTextColor.AQUA);

            if (!VersionInfo.isUnknown(ci.link())) {
                display.clickEvent(ClickEvent.openUrl(ci.link())).hoverEvent(GeneralMessage.HOVER_TEXT_CLICK_TO_OPEN_URL);
            }

            text.append(display);
        } else {
            text.append(Component.text("Unknown", NamedTextColor.RED));
        }

        return text.build();
    }

    private @NotNull Component formatBuildDate(@NotNull Instant buildDate) {
        var localDate = LocalDateTime.ofInstant(buildDate, ZoneId.systemDefault());
        return Components.aquaText(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

}
