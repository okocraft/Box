package net.okocraft.box.feature.command.boxadmin;

import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import dev.siroshun.codec4j.api.error.EncodeError;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.exporter.BoxDataFile;
import net.okocraft.box.storage.api.holder.StorageHolder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NotNullByDefault
public class ExportCommand extends AbstractCommand {

    public ExportCommand(DefaultMessageCollector collector) {
        super("export", "box.admin.command.export");
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return Component.empty();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        sender.sendPlainMessage("Exporting data...");

        Path path = BoxAPI.api().getPluginDirectory().resolve("data-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-") + "json.gz");

        BoxDataFile.encode(path, StorageHolder.getStorage(), BoxAPI.api().getItemManager(), BoxAPI.api().getEventCallers().sync())
            .inspect(ignored -> sender.sendPlainMessage("Exported to " + path.toAbsolutePath()))
            .inspectError(err -> {
                if (err instanceof EncodeError.FatalError fatalError) {
                    BoxLogger.logger().error("Failed to export data", fatalError.cause());
                } else {
                    BoxLogger.logger().error("Failed to export data: {}", err);
                }
                sender.sendPlainMessage("Failed to export data. Check your console.");
            });
    }
}
