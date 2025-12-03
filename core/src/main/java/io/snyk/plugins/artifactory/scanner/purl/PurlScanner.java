package io.snyk.plugins.artifactory.scanner.purl;

import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.MalwareCheck;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class PurlScanner {

  private static final Logger LOG = getLogger(PurlScanner.class);

  public PurlScanner() {
  }

  public TestResult scan(String packageName, String packageVersion, String ecosystem) {
    boolean isMalware = MalwareCheck.isMalware(packageName, packageVersion, ecosystem);
    TestResult testResult = new TestResult(isMalware);
    // TODO: Package Age check + add metadata for later filtering

    return testResult;
  }

}
