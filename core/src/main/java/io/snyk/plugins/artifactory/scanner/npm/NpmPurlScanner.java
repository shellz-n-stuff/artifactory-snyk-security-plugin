package io.snyk.plugins.artifactory.scanner.npm;

import io.snyk.plugins.artifactory.exception.CannotScanException;
import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.PackageScanner;
import io.snyk.plugins.artifactory.scanner.purl.PurlScanner;
import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class NpmPurlScanner implements PackageScanner {

  private static final Logger LOG = getLogger(NpmPurlScanner.class);
  private final PurlScanner purlScanner;

  public NpmPurlScanner(PurlScanner purlScanner) {
    this.purlScanner = purlScanner;
  }

  @Override
  public TestResult scan(FileLayoutInfo fileLayoutInfo, RepoPath repoPath) {
    LOG.debug("Node: repoPath.toString() {}", repoPath.toString());

    NpmPackage pckg = NpmPackage.parse(repoPath.toString())
      .orElseThrow(() -> new CannotScanException("Package details not provided."));

    return purlScanner.scan(pckg.getName(), pckg.getVersion(), "npm");
  }

}

