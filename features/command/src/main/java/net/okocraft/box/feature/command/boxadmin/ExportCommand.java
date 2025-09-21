package net.okocraft.box.feature.command.boxadmin;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.base.TagResolverBase;
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
import org.jetbrains.annotations.NotNullByDefault;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NotNullByDefault
public class ExportCommand extends AbstractCommand {

    private final MiniMessageBase exportStart;
    private final Arg1<Path> exportSuccess;
    private final MiniMessageBase exportFailure;
    private final MiniMessageBase help;

    public ExportCommand(DefaultMessageCollector collector) {
        super("export", "box.admin.command.export");
        this.exportStart = MiniMessageBase.messageKey(collector.add("box.command.boxadmin.export.start", "<gray>Exporting Box data..."));
        this.exportSuccess = Arg1.arg1(collector.add("box.command.boxadmin.export.success", "<gray>Box data has been exported to <aqua><filepath><gray>"), path -> TagResolverBase.component("filepath", Component.text(path.toAbsolutePath().toString())));
        this.exportFailure = MiniMessageBase.messageKey(collector.add("box.command.boxadmin.export.failure", "<red>Failed to export Box data. Please check your console log."));
        this.help = MiniMessageBase.messageKey(collector.add("box.command.boxadmin.export.help", "<aqua>/boxadmin export<dark_gray> - <gray>Exports all data of Box to the Json file"));
    }

    @Override
    public Component getHelp(MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);
        this.exportStart.source(msgSrc).send(sender);

        Path path = BoxAPI.api().getPluginDirectory().resolve("data-" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-") + ".json.gz");

        BoxDataFile.encode(path, StorageHolder.getStorage(), BoxAPI.api().getItemManager(), BoxAPI.api().getEventCallers().sync())
            .inspect(ignored -> this.exportSuccess.apply(path).source(msgSrc).send(sender))
            .inspectError(err -> {
                if (err instanceof EncodeError.FatalError fatalError) {
                    BoxLogger.logger().error("Failed to export data", fatalError.cause());
                } else {
                    BoxLogger.logger().error("Failed to export data: {}", err);
                }
                this.exportFailure.source(msgSrc).send(sender);
            });
    }
}
