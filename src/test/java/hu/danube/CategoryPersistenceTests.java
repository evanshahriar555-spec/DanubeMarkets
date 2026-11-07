package hu.danube;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Tests that category CRUD writes really reach H2 rather than only the current
 * JPA persistence context. The embedded database is isolated from the user's
 * file-backed homework database and each test rolls back automatically.
 *
 * These tests do not simulate restarting the JVM; the manual restart check
 * remains separate submission evidence.
 */
@DataJpaTest
class CategoryPersistenceTests {

  @Autowired CategoryRepository categories;

  @Autowired EntityManager entityManager;

  @Test
  void createdCategoryCanBeReadBackAfterClearingJpaCache() {
    categories.saveAndFlush(new Category("persist-create", new BigDecimal("12.50")));

    entityManager.clear();

    Category reloaded = categories.findById("persist-create").orElseThrow();
    assertThat(reloaded.cname).isEqualTo("persist-create");
    assertThat(reloaded.price).isEqualByComparingTo("12.50");
  }

  @Test
  void updatedCategoryPriceSurvivesClearingJpaCache() {
    categories.saveAndFlush(new Category("persist-update", new BigDecimal("10.00")));
    entityManager.clear();

    Category category = categories.findById("persist-update").orElseThrow();
    category.price = new BigDecimal("27.99");
    categories.saveAndFlush(category);
    entityManager.clear();

    assertThat(categories.findById("persist-update").orElseThrow().price)
        .isEqualByComparingTo("27.99");
  }

  @Test
  void deletionIsVisibleAfterClearingJpaCache() {
    categories.saveAndFlush(new Category("persist-delete", new BigDecimal("5.00")));
    entityManager.clear();

    categories.deleteById("persist-delete");
    categories.flush();
    entityManager.clear();

    assertThat(categories.findById("persist-delete")).isEmpty();
  }
}
