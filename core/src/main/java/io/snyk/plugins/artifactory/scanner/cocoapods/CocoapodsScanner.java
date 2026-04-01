package io.snyk.plugins.artifactory.scanner.cocoapods;

import io.snyk.plugins.artifactory.exception.CannotScanException;
import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.PackageScanner;
import io.snyk.plugins.artifactory.scanner.purl.PurlScanner;
import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class CocoapodsScanner implements PackageScanner {

  private static final Logger LOG = getLogger(CocoapodsScanner.class);
  private final PurlScanner purlScanner;

  public CocoapodsScanner(PurlScanner purlScanner) {
    this.purlScanner = purlScanner;
  }

  @Override
  public TestResult scan(FileLayoutInfo fileLayoutInfo, RepoPath repoPath) {
    LOG.debug("Cocoapods: repoPath.getName() {}", repoPath.getName());

    CocoapodsPackage pckg = CocoapodsPackage.parse(repoPath.getName())
      .orElseThrow(() -> new CannotScanException("Unexpected Cocoapods package name" + repoPath.getName()));

    return purlScanner.scan(pckg.getName(), pckg.getVersion(), "cocoapods");
  }

}

