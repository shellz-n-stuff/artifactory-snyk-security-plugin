package io.snyk.plugins.artifactory.configuration.properties;

public enum ArtifactProperty {
  TEST_TIMESTAMP("block_firewall.test.timestamp"),
  IS_MALWARE("block_firewall.is_malware"),
  PACKAGE_NAME("block_firewall.package.name"),
  PACKAGE_VERSION("block_firewall.package.version");


  private final String propertyKey;

  ArtifactProperty(String propertyKey) {
    this.propertyKey = propertyKey;
  }

  public String propertyKey() {
    return propertyKey;
  }
}
