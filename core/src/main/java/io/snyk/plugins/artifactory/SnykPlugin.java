package io.snyk.plugins.artifactory;

import io.snyk.plugins.artifactory.audit.AuditModule;
import io.snyk.plugins.artifactory.configuration.ConfigurationModule;
import io.snyk.plugins.artifactory.configuration.properties.ArtifactProperty;
import io.snyk.plugins.artifactory.exception.CannotScanException;
import io.snyk.plugins.artifactory.exception.SnykRuntimeException;
import io.snyk.plugins.artifactory.scanner.ScannerModule;
import io.snyk.plugins.artifactory.scanner.ScannerResolver;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.Repositories;
import org.artifactory.security.User;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.http.HttpRequest;
import java.util.Optional;
import java.util.Properties;

import static io.snyk.plugins.artifactory.configuration.PluginConfiguration.TEST_CONTINUOUSLY;
import static java.lang.String.format;

public class SnykPlugin {

  private static final Logger LOG = LoggerFactory.getLogger(SnykPlugin.class);

  private ConfigurationModule configurationModule;
  private AuditModule auditModule;
  private ScannerModule scannerModule;

  SnykPlugin() {
  }

  public SnykPlugin(@Nonnull Repositories repositories, File pluginsDirectory) {
    try {
      LOG.info("Loading and validating plugin properties...");
      Properties properties = PropertyLoader.loadProperties(pluginsDirectory);
      String pluginVersion = PropertyLoader.loadPluginVersion(pluginsDirectory);
      configurationModule = new ConfigurationModule(properties);
      validateConfiguration();

      auditModule = new AuditModule();
      ScannerResolver scannerResolver = ScannerResolver.setup(configurationModule);
      scannerModule = new ScannerModule(configurationModule, repositories, scannerResolver);

      LOG.info("Plugin version: {}", pluginVersion);
    } catch (Exception ex) {
      throw new SnykRuntimeException("Snyk plugin could not be initialized!", ex);
    }
  }

  /**
   * Logs update event for following artifact properties:
   * <ul>
   * <li>{@link ArtifactProperty#ISSUE_LICENSES_FORCE_DOWNLOAD}</li>
   * <li>{@link ArtifactProperty#ISSUE_LICENSES_FORCE_DOWNLOAD_INFO}</li>
   * <li>{@link ArtifactProperty#ISSUE_VULNERABILITIES_FORCE_DOWNLOAD}</li>
   * <li>{@link ArtifactProperty#ISSUE_VULNERABILITIES_FORCE_DOWNLOAD_INFO}</li>
   * </ul>
   * <p>
   * Extension point: {@code storage.afterPropertyCreate}.
   */
  public void handleAfterPropertyCreateEvent(User user, ItemInfo itemInfo, String propertyName, String[] propertyValues) {
    LOG.debug("Handle 'afterPropertyCreate' event for: {}", itemInfo);
    auditModule.logPropertyUpdate(user, itemInfo, propertyName, propertyValues);
  }

  /**
   * Invoked once when an artifact is first fetched from an external repository.
   * Runs Snyk test and persists the result in properties.
   * <p>
   * Extension point: {@code storage.afterCreate}.
   */
  public void handleAfterCreate(RepoPath repoPath) {
    LOG.debug("Handle 'afterCreate' event for: {}", repoPath);

    try {
      scannerModule.testArtifact(repoPath);
    } catch (CannotScanException e) {
      LOG.debug("Artifact cannot be scanned. {} {}", e.getMessage(), repoPath);
    } catch(Exception e) {
      String causeMessage = getCauseMessage(e);
      String message = format("An API call failed. %s %s", causeMessage, repoPath);
      LOG.error(message);
    }
  }

  /**
   * Filters access based on Snyk properties stored on the artifact.
   * When in continuous mode, may run an extra Snyk test to refresh the results.
   * <p>
   * Extension point: {@code download.beforeDownload}.
   */
  public void handleBeforeDownloadEvent(RepoPath repoPath) {
    LOG.debug("Handle 'beforeDownload' event for: {}", repoPath);

    try {
      scannerModule.filterAccess(repoPath);
    } catch (CannotScanException e) {
      LOG.debug("Artifact cannot be scanned. {} {}", e.getMessage(), repoPath);
    }
  }

  private String getCauseMessage(Throwable e) {
    return Optional.ofNullable(e.getCause())
      .map(Throwable::getMessage)
      .map(m -> e.getMessage() + " " + m)
      .orElseGet(e::getMessage);
  }

  private void validateConfiguration() {
    try {
      configurationModule.validate();
    } catch (Exception ex) {
      throw new SnykRuntimeException("Snyk Plugin Configuration is not valid!", ex);
    }

    LOG.debug("Snyk Plugin Configuration:");
    configurationModule.getPropertyEntries().stream()
      .map(entry -> entry.getKey() + "=" + entry.getValue())
      .sorted()
      .forEach(LOG::debug);
  }

  private boolean shouldTestContinuously() {
    return configurationModule.getPropertyOrDefault(TEST_CONTINUOUSLY).equals("true");
  }

  @NotNull
  static String sanitizeHeaders(HttpRequest request) {
    Optional<String> authorization = request.headers().firstValue("Authorization");
    if (authorization.isPresent()) {
      String header = authorization.get();
      if (header.contains("token") && header.length() > 10) {
        String maskedAuthHeader = header.substring(0, 10) + "...";
        return request.headers().toString().replace(header, maskedAuthHeader);
      }
    }
    return request.headers().toString();
  }
}
