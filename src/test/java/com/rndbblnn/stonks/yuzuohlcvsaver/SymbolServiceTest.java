package com.rndbblnn.stonks.yuzuohlcvsaver;


import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SymbolServiceTest extends BaseIntegrationTest {

  @Autowired
  private SymbolService symbolService;

  @Test
  public void testGetDailyCandles() {

    symbolService.getDailyCandles("TSLA",
            LocalDate.of(2022,10,28),
            LocalDate.of(2022,11,30))
        .stream()
        .forEach(candleDto -> {
          System.out.println(candleDto);
        });

  }

  @Test
  public void testGetIntradayCandles() {

    symbolService.getIntradayCandles("TSLA",
        LocalDate.of(2022,11,28),
        LocalDate.of(2022,11,30))
        .stream()
        .forEach(candleDto -> {
          System.out.println(candleDto);
        });

  }

}
