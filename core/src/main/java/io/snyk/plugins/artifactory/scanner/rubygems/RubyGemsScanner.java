package io.snyk.plugins.artifactory.scanner.rubygems;

import io.snyk.plugins.artifactory.exception.CannotScanException;
import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.PackageScanner;
import io.snyk.plugins.artifactory.scanner.SnykDetailsUrl;
import io.snyk.plugins.artifactory.scanner.purl.PurlScanner;
import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.repo.RepoPath;

public class RubyGemsScanner implements PackageScanner {

  private final PurlScanner purlScanner;

  public RubyGemsScanner(PurlScanner purlScanner) {
    this.purlScanner = purlScanner;
  }

  @Override
  public TestResult scan(FileLayoutInfo fileLayoutInfo, RepoPath repoPath) {
    RubyGemsPackage pckg = RubyGemsPackage.parse(repoPath.getName())
      .orElseThrow(() -> new CannotScanException("Unexpected Ruby Gems package name: " + repoPath.getName()));

    String purl = "pkg:gem/" + pckg.getName() + "@" + pckg.getVersion();

    String packageDetailsUrl = getModuleDetailsURL(pckg.getName(), pckg.getVersion());

    return purlScanner.scan(pckg.getName(), pckg.getVersion(), "gem");
  }

  public static String getModuleDetailsURL(String name, String version) {
    return SnykDetailsUrl.create("rubygems", name, version).toString();
  }
}
