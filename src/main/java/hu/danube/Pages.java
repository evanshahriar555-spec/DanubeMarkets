package hu.danube;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class Pages {
  final OandaService o;
  final MnbService m;
  final Calculator c;

  public Pages(OandaService o, MnbService m, Calculator c) {
    this.o = o;
    this.m = m;
    this.c = c;
  }

  @Value("${team.first}")
  String first;

  @Value("${team.second}")
  String second;

  @ModelAttribute
  void common(Model model) {
    model.addAttribute("first", first);
    model.addAttribute("second", second);
    model.addAttribute("configured", o.configured());
  }

  @GetMapping("/")
  String home() {
    return "home";
  }

  @GetMapping("/soap")
  String soap(
      @RequestParam(defaultValue = "EUR") String currency,
      @RequestParam(required = false) LocalDate start,
      @RequestParam(required = false) LocalDate end,
      Model model) {
    model.addAttribute("currency", currency);
    model.addAttribute("start", start == null ? LocalDate.now().minusDays(14) : start);
    model.addAttribute("end", end == null ? LocalDate.now() : end);
    if (start != null && end != null)
      try {
        var rates = m.rates(currency, start, end);
        model.addAttribute("rates", rates);
        model.addAttribute("dates", rates.stream().map(MnbService.Rate::date).toList());
        model.addAttribute(
            "values",
            rates.stream()
                .map(
                    r ->
                        r.value()
                            .divide(
                                BigDecimal.valueOf(r.unit()), 8, java.math.RoundingMode.HALF_UP))
                .toList());
      } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
      }
    return "soap";
  }

  @GetMapping("/forex/{page}")
  String forex(
      @PathVariable String page,
      @RequestParam(defaultValue = "EUR_USD") String instrument,
      @RequestParam(defaultValue = "D") String granularity,
      @RequestParam(defaultValue = "false") boolean load,
      Model model) {
    if (!List.of("account", "actprice", "histprice", "open", "pos", "close").contains(page))
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.NOT_FOUND);
    model.addAttribute("page", page);
    model.addAttribute("instrument", instrument);
    model.addAttribute("granularity", granularity);
    model.addAttribute(
        "instruments", List.of("EUR_USD", "GBP_USD", "USD_JPY", "AUD_USD", "USD_CHF"));
    if (o.configured())
      try {
        model.addAttribute("instruments", o.instruments());
      } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
      }
    try {
      switch (page) {
        case "account" -> model.addAttribute("account", o.account());
        case "pos" -> {
          model.addAttribute("positions", o.positions());
          model.addAttribute("trades", o.trades());
        }
        case "close" -> {
          if (o.configured()) model.addAttribute("trades", o.trades());
        }
        case "actprice" -> {
          if (load) model.addAttribute("prices", o.price(instrument));
        }
        case "histprice" -> {
          if (load) model.addAttribute("candles", o.history(instrument, granularity));
        }
      }
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
    }
    return "forex";
  }

  @PostMapping("/forex/open")
  String open(
      @RequestParam String instrument,
      @RequestParam long units,
      @RequestParam(defaultValue = "false") boolean confirm,
      RedirectAttributes a) {
    try {
      if (!confirm) throw new IllegalArgumentException("Confirm the practice order.");
      var r = o.open(instrument, units);
      a.addFlashAttribute("receipt", r.toPrettyString());
      a.addFlashAttribute(
          "message",
          r.has("orderFillTransaction")
              ? "Market order filled."
              : "Order was not filled; inspect the receipt.");
    } catch (Exception e) {
      a.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/forex/open";
  }

  @PostMapping("/forex/close")
  String close(
      @RequestParam String tradeId,
      @RequestParam(defaultValue = "false") boolean confirm,
      RedirectAttributes a) {
    try {
      if (!confirm) throw new IllegalArgumentException("Confirm closing the practice trade.");
      var r = o.close(tradeId);
      a.addFlashAttribute("receipt", r.toPrettyString());
      a.addFlashAttribute(
          "message",
          r.has("orderFillTransaction")
              ? "Trade closed."
              : "Trade was not closed; inspect the receipt.");
    } catch (Exception e) {
      a.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/forex/close";
  }

  @GetMapping("/test")
  String test() {
    return "test";
  }

  @PostMapping("/test")
  String calc(
      @RequestParam BigDecimal a,
      @RequestParam BigDecimal b,
      @RequestParam String operation,
      Model model) {
    try {
      model.addAttribute("result", c.calculate(a, b, operation));
    } catch (IllegalArgumentException e) {
      model.addAttribute("error", e.getMessage());
    }
    return "test";
  }

  @GetMapping("/react")
  String react() {
    return "react";
  }
}
