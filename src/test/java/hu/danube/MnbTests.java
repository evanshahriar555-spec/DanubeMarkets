package hu.danube;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class MnbTests {
  @Test
  void soapEnvelope() throws Exception {
    var r =
        MnbService.parse(
            "<s:Envelope"
                + " xmlns:s='http://schemas.xmlsoap.org/soap/envelope/'><s:Body><GetExchangeRatesResponse"
                + " xmlns='http://www.mnb.hu/webservices/'><GetExchangeRatesResult>&lt;MNBExchangeRates&gt;&lt;Day"
                + " date='2026-01-02'&gt;&lt;Rate curr='EUR'"
                + " unit='1'&gt;400,50&lt;/Rate&gt;&lt;/Day&gt;&lt;/MNBExchangeRates&gt;</GetExchangeRatesResult></GetExchangeRatesResponse></s:Body></s:Envelope>");
    assertEquals("400.50", r.getFirst().value().toString());
  }

  @Test
  void decimalAndUnit() throws Exception {
    var r =
        MnbService.parse(
                "<MNBExchangeRates><Day date='2026-01-02'><Rate curr='JPY'"
                    + " unit='100'>234,56</Rate></Day></MNBExchangeRates>")
            .getFirst();
    assertEquals(100, r.unit());
    assertEquals("234.56", r.value().toString());
  }

  @Test
  void rejectXxe() {
    assertThrows(
        Exception.class,
        () ->
            MnbService.parse(
                "<!DOCTYPE foo [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><foo>&x;</foo>"));
  }

  @Test
  void validEmptyResponseIsAllowedForNonBusinessDays() throws Exception {
    assertTrue(MnbService.parse("<MNBExchangeRates/>").isEmpty());
  }

  @Test
  void malformedPayloadIsNotTreatedAsEmptyRates() {
    assertThrows(
        IllegalArgumentException.class,
        () -> MnbService.parse("<UnexpectedResponse/>"));
  }

  @Test
  void soapFaultIsNotTreatedAsEmptyRates() {
    assertThrows(
        IllegalArgumentException.class,
        () -> MnbService.parse(
            "<s:Envelope xmlns:s='http://schemas.xmlsoap.org/soap/envelope/'>"
                + "<s:Body><s:Fault><faultstring>Unavailable</faultstring></s:Fault>"
                + "</s:Body></s:Envelope>"));
  }

  @Test
  void zeroAndNegativeUnitsAreRejected() {
    for (int unit : new int[] {0, -100}) {
      String xml = "<MNBExchangeRates><Day date='2026-01-02'><Rate curr='JPY'"
          + " unit='" + unit + "'>234,56</Rate></Day></MNBExchangeRates>";
      assertThrows(IllegalArgumentException.class, () -> MnbService.parse(xml));
    }
  }

  @Test
  void invalidDates() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MnbService()
                .rates(
                    "EUR", java.time.LocalDate.of(2026, 2, 1), java.time.LocalDate.of(2026, 1, 1)));
  }
}
