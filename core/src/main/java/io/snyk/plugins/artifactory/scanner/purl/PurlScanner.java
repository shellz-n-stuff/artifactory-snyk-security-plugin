package io.snyk.plugins.artifactory.scanner.purl;

import io.snyk.plugins.artifactory.exception.SnykAPIFailureException;
import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.MalwareCheck;
import io.snyk.plugins.artifactory.scanner.TestResultConverter;
import io.snyk.sdk.api.SnykClient;
import io.snyk.sdk.api.SnykResult;
import io.snyk.sdk.model.purl.PurlIssues;
import org.slf4j.Logger;

import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

public class PurlScanner {

  private static final Logger LOG = getLogger(PurlScanner.class);

  private final SnykClient snykClient;
  private final String orgId;

  public PurlScanner(SnykClient snykClient, String orgId) {
    this.snykClient = snykClient;
    this.orgId = orgId;
  }

  public TestResult scan(String packageName, String packageVersion, String ecosystem) {
    boolean isMalware = MalwareCheck.isMalware(packageName, packageVersion, ecosystem);
    TestResult testResult = new TestResult(isMalware);
    // TODO: Package Age check + add metadata for later filtering

    return testResult;
  }

}
