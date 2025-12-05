package io.snyk.plugins.artifactory.scanner.rubygems;

import org.artifactory.fs.FileLayoutInfo;
import org.artifactory.repo.RepoPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;

class RubyGemsScannerTest {

  RubyGemsScanner scanner;

  RepoPath repoPath;
  FileLayoutInfo fileLayoutInfo;

  @BeforeEach
  void setUp() throws Exception {
    String org = System.getenv("TEST_SNYK_ORG");
    Assertions.assertNotNull(org, "must not be null for test");

    repoPath = mock(RepoPath.class);
    fileLayoutInfo = mock(FileLayoutInfo.class);
  }


}
