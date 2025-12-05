package io.snyk.plugins.artifactory.scanner;

import io.snyk.plugins.artifactory.model.TestResult;
import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.repo.RepoPath;

import java.time.Instant;

public interface PackageScanner {
  TestResult scan(FileLayoutInfo fileLayoutInfo, RepoPath repoPath);

  //Instant getPackageReleaseDate(String packageName, String packageVersion);
}
