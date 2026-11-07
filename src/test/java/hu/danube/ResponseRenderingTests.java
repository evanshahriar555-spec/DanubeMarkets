package hu.danube;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:render;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
class ResponseRenderingTests {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @MockitoBean OandaService o;
  @MockitoBean MnbService m;

  @BeforeEach
  void setup() {
    when(o.configured()).thenReturn(true);
    when(o.instruments()).thenReturn(List.of("EUR_USD"));
  }

  @Test
  void priceTable() throws Exception {
    when(o.price("EUR_USD"))
        .thenReturn(
            json.readTree(
                "[{\"instrument\":\"EUR_USD\",\"closeoutBid\":\"1.1000\",\"closeoutAsk\":\"1.1002\",\"status\":\"tradeable\",\"time\":\"2026-01-02\"}]"));
    mvc.perform(get("/forex/actprice?load=true"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("1.1002")));
  }

  @Test
  void candleTable() throws Exception {
    when(o.history("EUR_USD", "D"))
        .thenReturn(
            json.readTree(
                "[{\"time\":\"2026-01-02\",\"mid\":{\"o\":\"1.1\",\"h\":\"1.2\",\"l\":\"1.0\",\"c\":\"1.15\"},\"volume\":500,\"complete\":true}]"));
    mvc.perform(get("/forex/histprice?load=true"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("1.15")));
  }

  @Test
  void positionsTable() throws Exception {
    when(o.positions())
        .thenReturn(
            json.readTree(
                "[{\"instrument\":\"EUR_USD\",\"long\":{\"units\":\"100\"},\"short\":{\"units\":\"0\"},\"unrealizedPL\":\"2.50\"}]"));
    when(o.trades())
        .thenReturn(
            json.readTree(
                "[{\"id\":\"123\",\"instrument\":\"EUR_USD\",\"currentUnits\":\"100\",\"price\":\"1.1\"}]"));
    mvc.perform(get("/forex/pos"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("2.50")));
  }

  @Test
  void soapChart() throws Exception {
    when(m.rates(anyString(), any(), any()))
        .thenReturn(
            List.of(new MnbService.Rate("2026-01-02", "JPY", 100, new BigDecimal("234.56"))));
    mvc.perform(get("/soap?currency=JPY&start=2026-01-01&end=2026-01-03"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("234.56")))
        .andExpect(content().string(containsString("2.34560000")));
  }
}
