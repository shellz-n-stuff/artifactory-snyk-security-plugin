package io.snyk.plugins.artifactory.scanner;

import io.snyk.plugins.artifactory.configuration.ConfigurationModule;
import io.snyk.plugins.artifactory.configuration.PluginConfiguration;
import io.snyk.plugins.artifactory.configuration.properties.ArtifactProperties;
import io.snyk.plugins.artifactory.configuration.properties.RepositoryArtifactProperties;
import io.snyk.plugins.artifactory.ecosystem.EcosystemResolver;
import io.snyk.plugins.artifactory.ecosystem.RepositoryMetadataEcosystemResolver;
import io.snyk.plugins.artifactory.model.MonitoredArtifact;
import io.snyk.plugins.artifactory.model.TestResult;
import org.artifactory.exception.CancelException;
import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.Repositories;
import org.artifactory.repo.RepositoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ScannerModule {
  private static final Logger LOG = LoggerFactory.getLogger(ScannerModule.class);
  private final ConfigurationModule configurationModule;
  private final Repositories repositories;
  private final EcosystemResolver ecosystemResolver;
  private final ScannerResolver scannerResolver;
  private final ArtifactResolver artifactResolver;

  public ScannerModule(ConfigurationModule configurationModule, @Nonnull Repositories repositories, ScannerResolver scannerResolver) {
    this.configurationModule = requireNonNull(configurationModule);
    this.repositories = requireNonNull(repositories);

    ecosystemResolver = new RepositoryMetadataEcosystemResolver(repositories);

    this.scannerResolver = scannerResolver;

    artifactResolver = shouldTestContinuously() ? new ArtifactCache(
      durationHoursProperty(PluginConfiguration.TEST_FREQUENCY_HOURS, configurationModule),
      durationHoursProperty(PluginConfiguration.EXTEND_TEST_DEADLINE_HOURS, configurationModule)
    ) : new ReadOnlyArtifactResolver();
  }

  public Optional<MonitoredArtifact> testArtifact(@Nonnull RepoPath repoPath) {
    if(skip(repoPath)) {
      LOG.debug("No ecosystem matching for {}, skipping.", repoPath);
      return Optional.empty();
    }
    return runTest(repoPath).map(artifact -> artifact.write(properties(repoPath)));
  }

  public void filterAccess(@Nonnull RepoPath repoPath) {
    if(skip(repoPath)) {
      LOG.debug("No ecosystem matching for {}, skipping.", repoPath);
      return;
    }

    // This will need:
    // 1. An exception list to allow for things like Log4Shell type emergency updates
    Instant artifactModifiedDate = getLastModifiedDate(repoPath);
    if (artifactModifiedDate == null) {
      LOG.error("No last modified time found for {}", repoPath.toPath());
      throw new CancelException("Artifact blocked due to unknown package age", 403);
    }
    Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
    boolean isRemote = isRemoteRepository(repoPath);
    LOG.debug(
      "firewall: Artifact {} modified date: {}, isRemote: {}, twoDaysAgo: {}",
      repoPath.toPath(),
      artifactModifiedDate,
      isRemote,
      twoDaysAgo
    );
    if (
      // artifact was changed recently IE less than 2 days
      artifactModifiedDate.isAfter(twoDaysAgo)
      && isRemote
    ) {
      LOG.warn(
        "firewall: Package {} is recent enough (modified date: {}) to block download.",
        repoPath.toPath(),
        artifactModifiedDate
      );
      LOG.warn("firewall: Blocking artifact {} due to age {}", repoPath.toPath(), artifactModifiedDate);
      // TODO: We need to code in exceptions for OSS stuff we maintain and actively use such as Misk
      throw new CancelException("Remote Artifact blocked due to package age less than 2 days", 403);
    }

    resolveArtifact(repoPath)
      .ifPresentOrElse(
        artifact -> filter(artifact, repoPath),
        () -> LOG.info("No vulnerability info found for {}", repoPath)
      );
  }

  private Optional<MonitoredArtifact> resolveArtifact(RepoPath repoPath) {
    return artifactResolver.get(properties(repoPath), () -> runTest(repoPath));
  }

  private ArtifactProperties properties(RepoPath repoPath) {
    return new RepositoryArtifactProperties(repoPath, repositories);
  }

  private @NotNull Optional<MonitoredArtifact> runTest(RepoPath repoPath) {
    return ecosystemResolver.getFor(repoPath)
      .flatMap(scannerResolver::getFor)
      .map(scanner -> runTestWith(scanner, repoPath));
  }

  private MonitoredArtifact runTestWith(PackageScanner scanner, RepoPath repoPath) {
    FileLayoutInfo fileLayoutInfo = repositories.getLayoutInfo(repoPath);
    TestResult testResult = scanner.scan(fileLayoutInfo, repoPath);
    return toMonitoredArtifact(testResult, repoPath);
  }

  private void filter(MonitoredArtifact artifact, RepoPath repoPath) {
    TestResult testResult = artifact.getTestResult();
    // If it has Malware then always block
    if(testResult.getIsMalware()) {
      throw new CancelException("Artifact blocked due to malware detection by OSV", 403);
    }

  }

  private @NotNull MonitoredArtifact toMonitoredArtifact(TestResult testResult, @NotNull RepoPath repoPath) {
    return new MonitoredArtifact(repoPath.toString(), testResult, getLastModifiedDate(repoPath));
  }

  private Instant getLastModifiedDate(RepoPath repoPath) {
    try {
      ItemInfo itemInfo = repositories.getItemInfo(repoPath);
      if (itemInfo != null) {
        Instant lastModified = Instant.ofEpochMilli(itemInfo.getLastModified());
        return lastModified;
      }
    } catch (Exception e) {
      LOG.debug("Could not retrieve last modified date for {}: {}", repoPath, e);
    }
    return null;
  }

  private boolean isRemoteRepository(RepoPath repoPath) {
    String repoKey = repoPath.getRepoKey();
    RepositoryConfiguration repoConfig = repositories.getRepositoryConfiguration(repoKey);
    if (repoConfig == null) {
      LOG.warn("Firewall: Repository configuration not found for repoKey: {}", repoKey);
      return false;
    }
    String repoType = repoConfig.getType();

    LOG.info("Firewall: Found repository type: {}", repoType);
    return repoType.equals("remote");
  }

  private boolean shouldTestContinuously() {
    return configurationModule.getPropertyOrDefault(PluginConfiguration.TEST_CONTINUOUSLY).equals("true");
  }

  private Duration durationHoursProperty(PluginConfiguration property, ConfigurationModule configurationModule) {
    return Duration.ofHours(Integer.parseInt(configurationModule.getPropertyOrDefault(property)));
  }

  private boolean skip(RepoPath repoPath) {
    return ecosystemResolver.getFor(repoPath).isEmpty();
  }
}
