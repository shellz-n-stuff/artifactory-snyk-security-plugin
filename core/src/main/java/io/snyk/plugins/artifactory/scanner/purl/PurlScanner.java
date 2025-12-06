package io.snyk.plugins.artifactory.scanner.purl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.MalwareCheck;
import org.slf4j.Logger;

import java.net.http.HttpClient;

import static org.slf4j.LoggerFactory.getLogger;

public class PurlScanner {

  private static final Logger LOG = getLogger(PurlScanner.class);

  // deps.dev v3 API
  private static final String DEPS_DEV_BASE =
    "https://api.deps.dev/v3/systems/%s/packages/%s/versions/%s";

  private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public PurlScanner() {
  }

  public TestResult scan(String packageName, String packageVersion, String ecosystem) {
    LOG.info(
      "DoingTest: package={}, version={}, ecosystem={}",
      packageName,
      packageVersion,
      ecosystem
    );

    // 1) Malware check (OSV)
    boolean isMalware = MalwareCheck.isMalware(packageName, packageVersion, ecosystem);
    TestResult testResult = new TestResult(isMalware, packageName, packageVersion);

    // Do other checks here (IE vulns or maintainer info) in the future

    return testResult;
  }

}
