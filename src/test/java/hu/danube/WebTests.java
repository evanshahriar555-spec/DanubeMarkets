package hu.danube;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
      "oanda.token=",
      "oanda.account="
    })
@AutoConfigureMockMvc
class WebTests {
  @Autowired MockMvc mvc;

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/",
        "/soap",
        "/test",
        "/react",
        "/forex/account",
        "/forex/actprice",
        "/forex/histprice",
        "/forex/open",
        "/forex/pos",
        "/forex/close"
      })
  void pages(String url) throws Exception {
    mvc.perform(get(url)).andExpect(status().isOk());
  }

  @Test
  void calculate() throws Exception {
    mvc.perform(
            post("/test").with(csrf()).param("a", "4").param("b", "2").param("operation", "divide"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("result", new java.math.BigDecimal("2")));
  }

  @Test
  void zero() throws Exception {
    mvc.perform(
            post("/test").with(csrf()).param("a", "4").param("b", "0").param("operation", "divide"))
        .andExpect(model().attribute("error", "Cannot divide by zero."));
  }

  @Test
  void invalidNumber() throws Exception {
    mvc.perform(
            post("/test").with(csrf()).param("a", "bad").param("b", "1").param("operation", "add"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void csrfRequired() throws Exception {
    mvc.perform(post("/forex/open").param("instrument", "EUR_USD").param("units", "10"))
        .andExpect(status().isForbidden());
  }

  @Test
  void crud() throws Exception {
    String row = "{\"cname\":\"test-category\",\"price\":777}";
    mvc.perform(post("/api/categories").with(csrf()).contentType("application/json").content(row))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/categories").with(csrf()).contentType("application/json").content(row))
        .andExpect(status().isConflict());
    mvc.perform(
            put("/api/categories/test-category")
                .with(csrf())
                .contentType("application/json")
                .content(row.replace("777", "888")))
        .andExpect(jsonPath("$.price").value(888));
    mvc.perform(delete("/api/categories/test-category").with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(delete("/api/categories/test-category").with(csrf()))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(strings = {" leading", "trailing ", " both ", "  "})
  void rejectsCategoryNamesWithOuterSpaces(String name) throws Exception {
    String payload = "{\"cname\":\"" + name + "\",\"price\":12.50}";
    mvc.perform(
            post("/api/categories")
                .with(csrf())
                .contentType("application/json")
                .content(payload))
        .andExpect(status().isBadRequest());
  }

  @Test
  void allowsSpacesInsideCategoryName() throws Exception {
    String payload = "{\"cname\":\"test pizza category\",\"price\":12.50}";
    mvc.perform(
            post("/api/categories")
                .with(csrf())
                .contentType("application/json")
                .content(payload))
        .andExpect(status().isCreated());
    mvc.perform(delete("/api/categories/test%20pizza%20category").with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  void rejectsRenameOfPrimaryKey() throws Exception {
    String original = "{\"cname\":\"rename-source\",\"price\":100}";
    mvc.perform(
            post("/api/categories")
                .with(csrf())
                .contentType("application/json")
                .content(original))
        .andExpect(status().isCreated());
    try {
      mvc.perform(
              put("/api/categories/rename-source")
                  .with(csrf())
                  .contentType("application/json")
                  .content("{\"cname\":\"rename-target\",\"price\":100}"))
          .andExpect(status().isBadRequest());
      mvc.perform(get("/api/categories"))
          .andExpect(jsonPath("$[?(@.cname=='rename-source')]").exists());
    } finally {
      mvc.perform(delete("/api/categories/rename-source").with(csrf()))
          .andExpect(status().isNoContent());
    }
  }

  @Test
  void invalidCategory() throws Exception {
    mvc.perform(
            post("/api/categories")
                .with(csrf())
                .contentType("application/json")
                .content("{\"cname\":\"bad\",\"price\":-1}"))
        .andExpect(status().isBadRequest());
  }
}
