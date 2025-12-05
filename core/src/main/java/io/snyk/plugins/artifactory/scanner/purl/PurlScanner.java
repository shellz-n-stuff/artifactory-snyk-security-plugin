package io.snyk.plugins.artifactory.scanner.purl;

import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.MalwareCheck;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    System.out.printf("DoingTest: package=%s, version=%s, ecosystem=%s%n",
      packageName, packageVersion, ecosystem);

    // 1) Malware check (OSV)
    boolean isMalware = MalwareCheck.isMalware(packageName, packageVersion, ecosystem);
    TestResult testResult = new TestResult(isMalware);

    // 2) Package age / release date (deps.dev)
    Instant releaseDate = getPackageReleaseDate(packageName, packageVersion, ecosystem);
    if (releaseDate != null) {
      LOG.debug("Package {}@{} (ecosystem={}) publishedAt={}",
        packageName, packageVersion, ecosystem, releaseDate);
      ZonedDateTime publishedAt = releaseDate.atZone(ZoneOffset.UTC);
      testResult.setPublishDate(publishedAt);
    } else {
      LOG.debug("No publish date available for {}@{} (ecosystem={})",
        packageName, packageVersion, ecosystem);
    }

    return testResult;
  }

  /**
   * General-purpose release date lookup using deps.dev.
   *
   * @param packageName    name in the given ecosystem (e.g. "lodash", "com.google.guava:guava")
   * @param packageVersion version string
   * @param ecosystem      logical ecosystem, mapped to deps.dev systems (e.g. "npm", "maven", "pypi")
   * @return Instant of publishedAt, or null if unavailable / error.
   */
  public Instant getPackageReleaseDate(String packageName, String packageVersion, String ecosystem) {
    String depsDevSystem = toDepsDevSystem(ecosystem);
    if (depsDevSystem == null) {
      LOG.debug("No deps.dev mapping for ecosystem '{}', skipping publish date lookup", ecosystem);
      return null;
    }

    String encodedName = URLEncoder.encode(packageName, StandardCharsets.UTF_8);
    String encodedVersion = URLEncoder.encode(packageVersion, StandardCharsets.UTF_8);

    String url = String.format(DEPS_DEV_BASE, depsDevSystem, encodedName, encodedVersion);
    LOG.debug("Fetching publish date from deps.dev: {}", url);

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .GET()
      .build();

    try {
      HttpResponse<String> resp = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

      if (resp.statusCode() >= 400) {
        LOG.warn("deps.dev returned HTTP {} for {}@{} (ecosystem={})",
          resp.statusCode(), packageName, packageVersion, ecosystem);
        return null;
      }

      DepsDevVersionResponse body =
        MAPPER.readValue(resp.body(), DepsDevVersionResponse.class);

      if (body.publishedAt == null || body.publishedAt.isBlank()) {
        LOG.debug("deps.dev did not include publishedAt for {}@{} (ecosystem={})",
          packageName, packageVersion, ecosystem);
        return null;
      }

      try {
        // publishedAt is ISO-8601, e.g. "2021-02-22T14:34:34Z"
        return Instant.parse(body.publishedAt);
      } catch (DateTimeParseException e) {
        LOG.warn("Failed to parse publishedAt '{}' from deps.dev for {}@{} (ecosystem={})",
          body.publishedAt, packageName, packageVersion, ecosystem, e);
        return null;
      }

    } catch (IOException | InterruptedException e) {
      LOG.warn("Failed to fetch deps.dev metadata for {}@{} (ecosystem={})",
        packageName, packageVersion, ecosystem, e);
      return null;
    }
  }

  /**
   * Map your logical ecosystem string to deps.dev "system" values.
   *
   * deps.dev systems include: NPM, MAVEN, PYPI, NUGET, CRATESIO, GO, RUBYGEMS, etc.
   */
  private String toDepsDevSystem(String ecosystem) {
    if (ecosystem == null) {
      return null;
    }
    switch (ecosystem.toLowerCase()) {
      case "npm":
      case "node":
        return "NPM";
      case "maven":
      case "java":
        return "MAVEN";
      case "pypi":
      case "python":
        return "PYPI";
      case "nuget":
      case "dotnet":
      case "net":
        return "NUGET";
      case "crates":
      case "crates.io":
      case "rust":
        return "CRATESIO";
      case "go":
      case "gomod":
      case "golang":
        return "GO";
      case "rubygems":
      case "ruby":
        return "RUBYGEMS";
      default:
        return null;
    }
  }

  // Minimal deps.dev version response model (v3)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DepsDevVersionResponse {
    public String publishedAt;
  }
}
