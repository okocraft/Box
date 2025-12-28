package net.okocraft.box.feature.command.boxadmin;

import dev.siroshun.codec4j.api.error.EncodeError;
import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.exporter.BoxDataFile;
import net.okocraft.box.storage.api.holder.StorageHolder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNullByDefault;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NotNullByDefault
public class ExportCommand extends AbstractCommand {

    private final MessageKey exportStart;
    private final MessageKey.Arg1<Path> exportSuccess;
    private final MessageKey exportFailure;
    private final MessageKey help;

    public ExportCommand(DefaultMessageCollector collector) {
        super("export", "box.admin.command.export");
        this.exportStart = MessageKey.key(collector.add("box.command.boxadmin.export.start", "<gray>Exporting Box data..."));
        this.exportSuccess = MessageKey.arg1(collector.add("box.command.boxadmin.export.success", "<gray>Box data has been exported to <aqua><filepath><gray>"), path -> Argument.string("filepath", path.toAbsolutePath().toString()));
        this.exportFailure = MessageKey.key(collector.add("box.command.boxadmin.export.failure", "<red>Failed to export Box data. Please check your console log."));
        this.help = MessageKey.key(collector.add("box.command.boxadmin.export.help", "<aqua>/boxadmin export<dark_gray> - <gray>Exports all data of Box to the Json file"));
    }

    @Override
    public ComponentLike getHelp() {
        return this.help;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage(this.exportStart);

        Path path = BoxAPI.api().getPluginDirectory().resolve("data-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-") + ".json.gz");

        BoxDataFile.encode(path, StorageHolder.getStorage(), BoxAPI.api().getItemManager(), BoxAPI.api().getEventCallers().sync())
            .inspect(_ -> sender.sendMessage(this.exportSuccess.apply(path)))
            .inspectError(err -> {
                if (err instanceof EncodeError.FatalError fatalError) {
                    BoxLogger.logger().error("Failed to export data", fatalError.cause());
                } else {
                    BoxLogger.logger().error("Failed to export data: {}", err);
                }
                sender.sendMessage(this.exportFailure);
            });
    }
}
