package io.snyk.plugins.artifactory.configuration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationModuleTest {

  private static Properties PROPERTIES;

  @BeforeAll
  static void setUpAll() {
    PROPERTIES = new Properties();
  }

}
