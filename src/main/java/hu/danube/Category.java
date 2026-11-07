package hu.danube;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
public class Category {
  @Id
  @NotBlank
  @Size(max = 60)
  // Match frontend trimming: allow spaces inside names, not at either end.
  @Pattern(regexp = "[A-Za-z0-9_-]+(?: +[A-Za-z0-9_-]+)*")
  public String cname;

  @NotNull
  @DecimalMin("0.01")
  @DecimalMax("1000000")
  public BigDecimal price;

  public Category() {}

  public Category(String n, BigDecimal p) {
    cname = n;
    price = p;
  }
}
