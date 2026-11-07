package hu.danube;

import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class CalculatorTests {
  Calculator c = new Calculator();

  @ParameterizedTest
  @CsvSource({
    "12,3,add,15",
    "12,3,subtract,9",
    "12,3,multiply,36",
    "12,3,divide,4",
    "0.1,0.2,add,0.3",
    "-2,3,multiply,-6",
    "1,3,divide,0.3333333333",
    "0,7,divide,0",
    "-5,-2,subtract,-3"
  })
  void arithmetic(String a, String b, String op, String expected) {
    assertEquals(
        0,
        new BigDecimal(expected).compareTo(c.calculate(new BigDecimal(a), new BigDecimal(b), op)));
  }

  @Test
  void zero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> c.calculate(BigDecimal.ONE, BigDecimal.ZERO, "divide"));
  }

  @Test
  void operation() {
    assertThrows(
        IllegalArgumentException.class, () -> c.calculate(BigDecimal.ONE, BigDecimal.ONE, "bad"));
  }
}
