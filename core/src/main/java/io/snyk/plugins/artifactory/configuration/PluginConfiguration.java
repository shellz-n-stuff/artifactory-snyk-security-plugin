package io.snyk.plugins.artifactory.configuration;

public enum PluginConfiguration implements Configuration {
  API_TIMEOUT("block_firewall.api.timeout", "60000"),
  HTTP_PROXY_HOST("block_firewall.http.proxyHost", ""),
  HTTP_PROXY_PORT("block_firewall.http.proxyPort", "80"),

  // scanner module
  SCANNER_PACKAGE_TYPE_MAVEN("block_firewall.scanner.packageType.maven", "true"),
  SCANNER_PACKAGE_TYPE_NPM("block_firewall.scanner.packageType.npm", "true"),
  SCANNER_PACKAGE_TYPE_PYPI("block_firewall.scanner.packageType.pypi", "true"),
  SCANNER_PACKAGE_TYPE_RUBYGEMS("block_firewall.scanner.packageType.gems", "true"),
  SCANNER_PACKAGE_TYPE_NUGET("block_firewall.scanner.packageType.nuget", "true"),
  SCANNER_PACKAGE_TYPE_COCOAPODS("block_firewall.scanner.packageType.cocoapods", "true"),
  SCANNER_BLOCK_ON_API_FAILURE("block_firewall.block-on-api-failure", "false"),
  TEST_CONTINUOUSLY("block_firewall.test.continuously","false"),
  TEST_FREQUENCY_HOURS("block_firewall.retest_frequency.hours", "168"),
  EXTEND_TEST_DEADLINE_HOURS("block_firewall.extendTestDeadline.hours", "24");

  private final String propertyKey;
  private final String defaultValue;

  PluginConfiguration(String propertyKey, String defaultValue) {
    this.propertyKey = propertyKey;
    this.defaultValue = defaultValue;
  }

  @Override
  public String propertyKey() {
    return propertyKey;
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }
}
