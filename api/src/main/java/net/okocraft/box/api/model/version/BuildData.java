package net.okocraft.box.api.model.version;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * An interface to represent build information.
 */
@ApiStatus.Experimental
public sealed interface BuildData permits BuildData.CI, BuildData.Local {

    /**
     * Returns the {@link Instant} representing when Box was built.
     * <p>
     * If the build date is unknown, this method returns {@link Instant#MIN}.
     *
     * @return the {@link Instant} representing when Box was built
     */
    @NotNull Instant buildDate();

    /**
     * Returns the latest commit hash in Git when Box was built.
     *
     * @return the latest commit hash in Git when Box was built
     */
    @NotNull String commitHash();

    /**
     * A extended {@link BuildData} that adds CI information.
     */
    non-sealed interface CI extends BuildData {

        /**
         * Returns the name of the service.
         *
         * @return the name of the service
         */
        @NotNull String serviceName();

        /**
         * Returns the number of build.
         *
         * @return the number of build
         */
        @NotNull String buildNumber();

        /**
         * Returns the link to the CI page where Box was built.
         *
         * @return the link to the CI page where Box was built.
         */
        @NotNull String link();
    }

    /**
     * A {@link BuildData} representing that Box was built locally.
     *
     * @param buildDate  the {@link Instant} representing when Box was built
     * @param commitHash the latest commit hash in Git when Box was built
     */
    record Local(@NotNull Instant buildDate, @NotNull String commitHash) implements BuildData {
    }

    /**
     * A {@link BuildData} representing that Box was built with GitHub Actions.
     *
     * @param buildDate  the {@link Instant} representing when Box was built
     * @param commitHash the latest commit hash in Git when Box was built
     * @param buildNumber the number of build
     * @param repository the repository name
     * @param runId the run id
     */
    record GitHubActions(@NotNull Instant buildDate, @NotNull String commitHash,
                         @NotNull String buildNumber,
                         @NotNull String repository, @NotNull String runId) implements CI {

        /**
         * A {@link String} represents "GitHub Actions".
         */
        public static final String SERVICE_NAME = "GitHub Actions";

        @Override
        public @NotNull String serviceName() {
            return SERVICE_NAME;
        }

        @Override
        public @NotNull String link() {
            if (VersionInfo.isUnknown(this.repository) || VersionInfo.isUnknown(this.runId)) {
                return VersionInfo.UNKNOWN_VALUE;
            }
            return "https://github.com/" + this.repository + "/actions/runs/" + this.runId;
        }
    }
}
