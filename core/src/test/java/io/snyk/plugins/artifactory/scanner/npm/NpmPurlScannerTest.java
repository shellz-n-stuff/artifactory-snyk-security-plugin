package io.snyk.plugins.artifactory.scanner.npm;

import io.snyk.plugins.artifactory.model.TestResult;
import io.snyk.plugins.artifactory.scanner.purl.PurlScanner;
import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.repo.RepoPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NpmPurlScannerTest {

  NpmPurlScanner scanner;

  RepoPath repoPath;
  FileLayoutInfo fileLayoutInfo;

  @BeforeEach
  void setUp() throws Exception {

    scanner = new NpmPurlScanner(new PurlScanner());

    repoPath = mock(RepoPath.class);
    fileLayoutInfo = mock(FileLayoutInfo.class);
  }

  @Test
  void testScanningMalwarePackage() {
    when(repoPath.toString()).thenReturn("npm:eslint-config-prettier/-/eslint-config-prettier-8.10.1.tgz");
    TestResult result = scanner.scan(fileLayoutInfo, repoPath);

    assertThat(result.getIsMalware()).isEqualTo(true);
  }

  @Test
  void testScanningNonMalwarePackage() {
    // Example: lodash 4.17.21 (clean)
    when(repoPath.toString()).thenReturn("npm:lodash/-/lodash-4.17.21.tgz");

    TestResult result = scanner.scan(fileLayoutInfo, repoPath);

    assertThat(result.getIsMalware()).isEqualTo(false);
    //assertThat(result.getPublishDate().toString()).isEqualTo("2021-02-20T15:42:16Z");
  }



}

