package hu.danube;

import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OandaService {
  private final String token, account;
  private final ObjectMapper json;
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  public OandaService(
      @Value("${oanda.token}") String t, @Value("${oanda.account}") String a, ObjectMapper j) {
    token = t;
    account = a;
    json = j;
  }

  public boolean configured() {
    return !token.isBlank() && !account.isBlank();
  }

  public JsonNode request(String method, String path, Object body) {
    if (!configured())
      throw new IllegalStateException(
          "Set OANDA_TOKEN and OANDA_ACCOUNT_ID for a v20 practice account, then restart.");
    if (!account.matches("[0-9-]+"))
      throw new IllegalArgumentException("Invalid account ID format.");
    try {
      var builder =
          HttpRequest.newBuilder(URI.create("https://api-fxpractice.oanda.com/v3" + path))
              .timeout(Duration.ofSeconds(20))
              .header("Authorization", "Bearer " + token)
              .header("Content-Type", "application/json");
      builder.method(
          method,
          body == null
              ? HttpRequest.BodyPublishers.noBody()
              : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
      var r = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      var data = json.readTree(r.body());
      if (r.statusCode() >= 400)
        throw new IllegalStateException(
            "OANDA HTTP "
                + r.statusCode()
                + ": "
                + data.path("errorMessage").asText("Request failed"));
      return data;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Request interrupted");
    } catch (java.io.IOException e) {
      throw new IllegalStateException("OANDA connection failed; check network and credentials.");
    }
  }

  String base() {
    return "/accounts/" + account;
  }

  static JsonNode requiredObject(JsonNode response, String field) {
    JsonNode value = response == null ? null : response.get(field);
    if (value == null || !value.isObject())
      throw new IllegalStateException("Invalid OANDA response: missing or invalid " + field + " object.");
    return value;
  }

  static JsonNode requiredArray(JsonNode response, String field) {
    JsonNode value = response == null ? null : response.get(field);
    if (value == null || !value.isArray())
      throw new IllegalStateException("Invalid OANDA response: missing or invalid " + field + " array.");
    return value;
  }

  public JsonNode account() {
    return requiredObject(request("GET", base() + "/summary", null), "account");
  }

  public List<String> instruments() {
    var out = new ArrayList<String>();
    requiredArray(request("GET", base() + "/instruments", null), "instruments")
        .forEach(n -> out.add(n.path("name").asText()));
    Collections.sort(out);
    return out;
  }

  public static void instrument(String s) {
    if (s == null || !s.matches("[A-Z0-9]{2,12}_[A-Z0-9]{2,12}"))
      throw new IllegalArgumentException("Invalid instrument.");
  }

  public JsonNode price(String s) {
    instrument(s);
    return requiredArray(request("GET", base() + "/pricing?instruments=" + s, null), "prices");
  }

  public JsonNode history(String s, String g) {
    instrument(s);
    if (!List.of("M1", "M5", "M15", "M30", "H1", "H4", "D", "W", "M").contains(g))
      throw new IllegalArgumentException("Invalid granularity.");
    return requiredArray(
        request("GET", "/instruments/" + s + "/candles?count=10&price=M&granularity=" + g, null),
        "candles");
  }

  public JsonNode positions() {
    return requiredArray(request("GET", base() + "/openPositions", null), "positions");
  }

  public JsonNode trades() {
    return requiredArray(request("GET", base() + "/openTrades", null), "trades");
  }

  public JsonNode open(String s, long units) {
    instrument(s);
    if (units == 0 || units > 100000 || units < -100000)
      throw new IllegalArgumentException("Units must be nonzero and between -100000 and 100000.");
    return request(
        "POST",
        base() + "/orders",
        Map.of(
            "order",
            Map.of(
                "type",
                "MARKET",
                "instrument",
                s,
                "units",
                Long.toString(units),
                "timeInForce",
                "FOK",
                "positionFill",
                "DEFAULT")));
  }

  public JsonNode close(String id) {
    if (id == null || !id.matches("[1-9][0-9]{0,19}"))
      throw new IllegalArgumentException("Enter a positive numeric trade ID.");
    return request("PUT", base() + "/trades/" + id + "/close", Map.of("units", "ALL"));
  }
}
