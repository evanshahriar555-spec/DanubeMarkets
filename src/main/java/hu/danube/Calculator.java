package hu.danube;

import java.math.*;
import org.springframework.stereotype.Service;

@Service
public class Calculator {
  public BigDecimal calculate(BigDecimal a, BigDecimal b, String op) {
    return switch (op) {
      case "add" -> a.add(b);
      case "subtract" -> a.subtract(b);
      case "multiply" -> a.multiply(b);
      case "divide" -> {
        if (b.signum() == 0) throw new IllegalArgumentException("Cannot divide by zero.");
        // stripTrailingZeros may represent whole numbers as 1E+3 instead of 1000.
        // Keep a nonnegative scale so calculator results display in plain notation.
        BigDecimal result = a.divide(b, 10, RoundingMode.HALF_UP).stripTrailingZeros();
        yield result.scale() < 0 ? result.setScale(0) : result;
      }
      default -> throw new IllegalArgumentException("Unknown operation.");
    };
  }
}
