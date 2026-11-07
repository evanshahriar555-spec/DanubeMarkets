package hu.danube;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

class OandaValidationTests {
  OandaService o = new OandaService("", "", new ObjectMapper());

  @Test
  void validEmptyArraysRemainValid() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    var response = mapper.readTree("{\"prices\":[],\"trades\":[],\"positions\":[]}");
    assertTrue(OandaService.requiredArray(response, "prices").isEmpty());
    assertTrue(OandaService.requiredArray(response, "trades").isEmpty());
    assertTrue(OandaService.requiredArray(response, "positions").isEmpty());
  }

  @Test
  void validAccountObjectIsAccepted() throws Exception {
    var response = new ObjectMapper().readTree("{\"account\":{\"currency\":\"USD\"}}");
    assertEquals("USD", OandaService.requiredObject(response, "account").get("currency").asText());
  }

  @Test
  void missingAndWronglyTypedArraysAreRejected() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    assertThrows(IllegalStateException.class,
        () -> OandaService.requiredArray(mapper.readTree("{}"), "prices"));
    assertThrows(IllegalStateException.class,
        () -> OandaService.requiredArray(mapper.readTree("{\"prices\":null}"), "prices"));
    assertThrows(IllegalStateException.class,
        () -> OandaService.requiredArray(mapper.readTree("{\"prices\":{}}"), "prices"));
    assertThrows(IllegalStateException.class,
        () -> OandaService.requiredArray(null, "prices"));
  }

  @Test
  void missingAndWronglyTypedAccountAreRejected() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    assertThrows(IllegalStateException.class,
        () -> OandaService.requiredObject(mapper.readTree("{}"), "account"));
    assertThrows(IllegalStateException.class,
        () -> OandaService.requiredObject(mapper.readTree("{\"account\":[]}"), "account"));
    assertThrows(IllegalStateException.class,
        () -> OandaService.requiredObject(mapper.readTree("{\"account\":null}"), "account"));
    assertThrows(IllegalStateException.class,
        () -> OandaService.requiredObject(null, "account"));
  }

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
