package io.snyk.plugins.artifactory.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.snyk.plugins.artifactory.configuration.PluginConfiguration.SCANNER_BLOCK_ON_API_FAILURE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PluginConfiguration")
class PluginConfigurationTest {

  @DisplayName("check default values")
  @Test
  void checkDefaultValues() {
    assertAll("should be not empty",
              () -> assertEquals("false", SCANNER_BLOCK_ON_API_FAILURE.defaultValue(), getAssertionMessage(SCANNER_BLOCK_ON_API_FAILURE, "default value must be 'false'"))
    );

  }

  private String getAssertionMessage(Configuration entry, String message) {
    return String.format("'%s' %s", entry.propertyKey(), message);
  }
}
