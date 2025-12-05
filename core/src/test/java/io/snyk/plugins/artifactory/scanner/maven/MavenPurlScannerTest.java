package io.snyk.plugins.artifactory.scanner.maven;

import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.repo.RepoPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;

class MavenPurlScannerTest {

  MavenPurlScanner scanner;

  RepoPath repoPath;
  FileLayoutInfo fileLayoutInfo;

  @BeforeEach
  void setUp() throws Exception {
    String org = System.getenv("TEST_SNYK_ORG");
    Assertions.assertNotNull(org, "must not be null for test");

    //scanner = new MavenPurlScanner(new PurlScanner(snykClient, org));

    repoPath = mock(RepoPath.class);
    fileLayoutInfo = mock(FileLayoutInfo.class);
  }

}

