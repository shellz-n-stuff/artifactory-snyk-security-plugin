package io.snyk.plugins.artifactory.model;

import io.snyk.plugins.artifactory.configuration.properties.ArtifactProperties;
import io.snyk.plugins.artifactory.configuration.properties.ArtifactProperty;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class MonitoredArtifact {

  private static final Logger LOG = getLogger(MonitoredArtifact.class);

  private final String path;

  private TestResult testResult;


  private final Instant lastModifiedDate;

  public MonitoredArtifact(String path, TestResult testResult) {
    this(path, testResult, null);
  }

  public MonitoredArtifact(String path, TestResult testResult, Instant lastModifiedDate) {
    this.path = path;
    this.testResult = testResult;
    this.lastModifiedDate = lastModifiedDate;
  }

  public String getPath() {
    return path;
  }

  public TestResult getTestResult() {
    return testResult;
  }

  public MonitoredArtifact write(ArtifactProperties properties) {
    testResult.write(properties);
    return this;
  }

  private void setDefaultArtifactProperty(ArtifactProperties properties, ArtifactProperty property, String value) {
    if (!properties.has(property)) {
      properties.set(property, value);
    }
  }

  // Purely used for cases where we are checking if it needs another test
  public static Optional<MonitoredArtifact> read(ArtifactProperties properties) {
    try {
      return TestResult.read(properties).map(testResult ->
        new MonitoredArtifact(
          properties.getArtifactPath(),
          testResult
        )
      );
    } catch (RuntimeException e) {
      LOG.error("Failed to read artifact properties of artifact {}. Error: {}", properties.getArtifactPath(), e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MonitoredArtifact artifact = (MonitoredArtifact) o;
    return Objects.equals(path, artifact.path) && Objects.equals(testResult, artifact.testResult) && Objects.equals(lastModifiedDate, artifact.lastModifiedDate);
  }

  public Optional<Instant> getLastModifiedDate() {
    return Optional.ofNullable(lastModifiedDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, testResult, lastModifiedDate);
  }

  @Override
  public String toString() {
    return "MonitoredArtifact{" +
      "path='" + path + '\'' +
      ", testResult=" + testResult +
      ", lastModifiedDate=" + lastModifiedDate +
      '}';
  }
}
