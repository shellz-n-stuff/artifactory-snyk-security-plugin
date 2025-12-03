package io.snyk.plugins.artifactory.scanner.maven;

import io.snyk.plugins.artifactory.exception.CannotScanException;
import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.PackageScanner;
import io.snyk.plugins.artifactory.scanner.purl.PurlScanner;
import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class MavenPurlScanner implements PackageScanner {

  private static final Logger LOG = getLogger(MavenPurlScanner.class);
  private final PurlScanner purlScanner;

  public MavenPurlScanner(PurlScanner purlScanner) {
    this.purlScanner = purlScanner;
  }

  @Override
  public TestResult scan(FileLayoutInfo fileLayoutInfo, RepoPath repoPath) {
    LOG.debug("Maven: repoPath.getName() {}", repoPath.getName());

    MavenPackage pckg = MavenPackage.parse(fileLayoutInfo)
      .orElseThrow(() -> new CannotScanException("Maven package details not provided"));

    String purl = "pkg:maven/" + pckg.getName() + "@" + pckg.getVersion();

    return purlScanner.scan(pckg.getName(), pckg.getVersion(), "maven");
  }

}

