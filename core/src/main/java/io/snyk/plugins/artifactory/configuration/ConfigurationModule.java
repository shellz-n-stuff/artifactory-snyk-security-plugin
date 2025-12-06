package io.snyk.plugins.artifactory.configuration;

import javax.annotation.Nonnull;
import java.util.*;

public class ConfigurationModule {

  private final Properties properties;

  public ConfigurationModule(@Nonnull Properties properties) {
    this.properties = properties;
  }

  public Set<Map.Entry<Object, Object>> getPropertyEntries() {
    return new HashSet<>(properties.entrySet());
  }

  public String getProperty(Configuration config) {
    return properties.getProperty(config.propertyKey());
  }

  public String getPropertyOrDefault(Configuration config) {
    return properties.getProperty(config.propertyKey(), config.defaultValue());
  }

  public void validate() {

    List<String> allowListedPackages;
  }
}
