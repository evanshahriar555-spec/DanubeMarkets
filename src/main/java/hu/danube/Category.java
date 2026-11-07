package hu.danube;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
public class Category {
  @Id
  @NotBlank
  @Size(max = 60)
  @Pattern(regexp = "[A-Za-z0-9 _-]+")
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
