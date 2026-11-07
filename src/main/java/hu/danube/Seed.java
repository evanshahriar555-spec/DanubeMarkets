package hu.danube;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class Seed {
  @Bean
  CommandLineRunner initializeCategories(JdbcTemplate j) {
    return args -> {
      j.execute("CREATE TABLE IF NOT EXISTS seed_history (id INT PRIMARY KEY)");
      if (j.queryForObject("SELECT COUNT(*) FROM seed_history WHERE id=1", Integer.class) == 0) {
        j.update(
            "MERGE INTO category(cname,price) KEY(cname) VALUES"
                + " ('page',850),('nobleman',950),('king',1250),('knight',1150)");
        j.update("INSERT INTO seed_history VALUES (1)");
      }
    };
  }
}
