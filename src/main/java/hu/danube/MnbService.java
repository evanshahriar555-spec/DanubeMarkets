package hu.danube;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import javax.xml.parsers.*;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

@Service
public class MnbService {
  public record Rate(String date, String currency, int unit, BigDecimal value) {}

  static Document xml(String text) throws Exception {
    var f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true);
    f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    f.setFeature("http://xml.org/sax/features/external-general-entities", false);
    f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    return f.newDocumentBuilder().parse(new InputSource(new StringReader(text)));
  }

  public static List<Rate> parse(String text) throws Exception {
    var doc = xml(text);
    var result = doc.getElementsByTagNameNS("*", "GetExchangeRatesResult");
    if (result.getLength() > 0) doc = xml(result.item(0).getTextContent());
    // A malformed or fault-only SOAP response must not look like a valid empty
    // exchange-rate interval (e.g. weekends or public holidays).
    var root = doc.getDocumentElement();
    if (!"MNBExchangeRates".equals(root.getLocalName())
        && !"MNBExchangeRates".equals(root.getNodeName()))
      throw new IllegalArgumentException("Invalid MNB exchange-rate response.");
    var days = doc.getElementsByTagName("Day");
    var list = new ArrayList<Rate>();
    for (int i = 0; i < days.getLength(); i++) {
      var day = (Element) days.item(i);
      var rs = day.getElementsByTagName("Rate");
      for (int k = 0; k < rs.getLength(); k++) {
        var r = (Element) rs.item(k);
        int unit = Integer.parseInt(r.getAttribute("unit"));
        if (unit <= 0) throw new IllegalArgumentException("Invalid MNB rate unit.");
        list.add(
            new Rate(
                day.getAttribute("date"),
                r.getAttribute("curr"),
                unit,
                new BigDecimal(r.getTextContent().trim().replace(',', '.'))));
      }
    }
    list.sort(Comparator.comparing(Rate::date));
    return list;
  }

  public List<Rate> rates(String currency, LocalDate start, LocalDate end) {
    if (!List.of("EUR", "USD", "GBP", "CHF", "JPY", "PLN", "CZK", "AUD", "CAD").contains(currency))
      throw new IllegalArgumentException("Unsupported currency.");
    if (start.isAfter(end) || start.plusYears(1).isBefore(end))
      throw new IllegalArgumentException("Choose an ordered date range of at most one year.");
    String envelope =
        "<soap:Envelope"
            + " xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><GetExchangeRates"
            + " xmlns=\"http://www.mnb.hu/webservices/\"><startDate>"
            + start
            + "</startDate><endDate>"
            + end
            + "</endDate><currencyNames>"
            + currency
            + "</currencyNames></GetExchangeRates></soap:Body></soap:Envelope>";
    try {
      var req =
          HttpRequest.newBuilder(URI.create("http://www.mnb.hu/arfolyamok.asmx"))
              .timeout(Duration.ofSeconds(25))
              .header("Content-Type", "text/xml; charset=utf-8")
              .header(
                  "SOAPAction",
                  "\"http://www.mnb.hu/webservices/MNBArfolyamServiceSoap/GetExchangeRates\"")
              .POST(HttpRequest.BodyPublishers.ofString(envelope))
              .build();
      var r =
          HttpClient.newBuilder()
              .connectTimeout(Duration.ofSeconds(10))
              .build()
              .send(req, HttpResponse.BodyHandlers.ofString());
      if (r.statusCode() != 200) throw new IllegalStateException("MNB HTTP " + r.statusCode());
      var d = xml(r.body());
      if (d.getElementsByTagNameNS("*", "Fault").getLength() > 0)
        throw new IllegalStateException("MNB SOAP fault.");
      return parse(r.body());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("MNB request interrupted");
    } catch (Exception e) {
      throw new IllegalStateException("MNB request failed: " + e.getMessage());
    }
  }
}
