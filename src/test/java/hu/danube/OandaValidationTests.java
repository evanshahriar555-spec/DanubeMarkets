package hu.danube;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

class OandaValidationTests {
  OandaService o = new OandaService("", "", new ObjectMapper());

  @Test
  void missingCredentials() {
    assertThrows(IllegalStateException.class, () -> o.account());
  }

  @Test
  void zeroUnits() {
    assertThrows(IllegalArgumentException.class, () -> o.open("EUR_USD", 0));
  }

  @Test
  void excessiveUnits() {
    assertThrows(IllegalArgumentException.class, () -> o.open("EUR_USD", 100001));
  }

  @Test
  void invalidTrade() {
    assertThrows(IllegalArgumentException.class, () -> o.close("../123"));
  }

  @Test
  void invalidGranularity() {
    assertThrows(IllegalArgumentException.class, () -> o.history("EUR_USD", "XYZ"));
  }

  @Test
  void invalidInstrument() {
    assertThrows(IllegalArgumentException.class, () -> o.price("EUR_USD&extra=true"));
  }
}
