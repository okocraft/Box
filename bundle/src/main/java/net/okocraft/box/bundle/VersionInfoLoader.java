package net.okocraft.box.bundle;

import net.okocraft.box.api.model.version.BuildData;
import net.okocraft.box.api.model.version.VersionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Properties;
import java.util.function.UnaryOperator;

public final class VersionInfoLoader {

    private final UnaryOperator<@Nullable String> valueGetter;

    public static @NotNull VersionInfo loadFromProperties(@NotNull Properties properties) {
        return new VersionInfoLoader(properties::getProperty).load();
    }

    private VersionInfoLoader(@NotNull UnaryOperator<@Nullable String> valueGetter) {
        this.valueGetter = valueGetter;
    }

    private @NotNull VersionInfo load() {
        return new VersionInfo(
                this.getValue("version"),
                Boolean.parseBoolean(this.getValue("isReleaseVersion")),
                this.loadBuildData()
        );
    }

    private @NotNull BuildData loadBuildData() {
        var buildDate = toInstant(this.getValue("buildDate"));
        var commitHash = this.getValue("commitHash");

        if (this.getValue("ci.serviceName").equals(BuildData.GitHubActions.SERVICE_NAME)) {
            return this.loadGitHubActionsInfo(buildDate, commitHash);
        } else {
            return new BuildData.Local(buildDate, commitHash);
        }
    }

    private @NotNull BuildData loadGitHubActionsInfo(@NotNull Instant buildDate, @NotNull String commitHash) {
        return new BuildData.GitHubActions(
                buildDate,
                commitHash,
                this.getValue("ci.buildNumber"),
                this.getValue("ci.repository"),
                this.getValue("ci.runId")
        );
    }

    private @NotNull String getValue(@NotNull String key) {
        var value = this.valueGetter.apply(key);
        return value != null ? value : VersionInfo.UNKNOWN_VALUE;
    }

    private @NotNull Instant toInstant(@NotNull String value) {
        try {
            return Instant.ofEpochMilli(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return Instant.MIN;
        }
    }
}
