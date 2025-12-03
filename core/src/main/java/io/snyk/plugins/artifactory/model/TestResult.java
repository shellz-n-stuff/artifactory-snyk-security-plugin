package io.snyk.plugins.artifactory.model;

import io.snyk.plugins.artifactory.configuration.properties.ArtifactProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import static io.snyk.plugins.artifactory.configuration.properties.ArtifactProperty.*;

public class TestResult {
  private static final Logger LOG = LoggerFactory.getLogger(TestResult.class);

  private final ZonedDateTime timestamp;
  // This can change!
  private boolean isMalware = false;
  private ZonedDateTime publishDate = null;

  public TestResult(boolean isMalware) {
    this(ZonedDateTime.now(), isMalware);
  }

  public TestResult(ZonedDateTime timestamp, boolean isMalware) {
    this.timestamp = timestamp;
    this.isMalware = isMalware;
  }


  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  public void write(ArtifactProperties properties) {
    LOG.info("Writing Snyk properties for package, artifactory path {}", properties.getArtifactPath());
    properties.set(TEST_TIMESTAMP, timestamp.toString());
    properties.set(IS_MALWARE, isMalware ? "true" : "false");
  }

  public static Optional<TestResult> read(ArtifactProperties properties) {
    Optional<ZonedDateTime> timestamp = properties.get(TEST_TIMESTAMP).map(ZonedDateTime::parse);
    String isPackageMalwareStr = String.valueOf(properties.get(IS_MALWARE));

    if (timestamp.isEmpty() || isPackageMalwareStr.isEmpty()) {
      return Optional.empty();
    }
    boolean isMalwarePackage = Boolean.parseBoolean(isPackageMalwareStr);
    return Optional.of(new TestResult(
      timestamp.get(),
      isMalwarePackage
    ));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TestResult that = (TestResult) o;
    return Objects.equals(timestamp, that.timestamp) && Objects.equals(isMalware, that.isMalware) && Objects.equals(publishDate, that.publishDate) ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, isMalware);
  }

  @Override
  public String toString() {
    return "TestResult{" +
      "timestamp=" + timestamp +
      ", isMalware=" + isMalware +
      '}';
  }

  public ZonedDateTime getPublishDate() {
    return publishDate;
  }

  public void setPublishDate(ZonedDateTime publishDate) {
    this.publishDate = publishDate;
  }

  public boolean getIsMalware() {
    return this.isMalware;
  }

  public void setIsMalware(boolean isMalware) {
    this.isMalware = isMalware;
  }
}
