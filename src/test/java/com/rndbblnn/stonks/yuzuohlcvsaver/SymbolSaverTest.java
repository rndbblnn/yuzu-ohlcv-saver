package com.rndbblnn.stonks.yuzuohlcvsaver;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

public class SymbolSaverTest extends BaseIntegrationTest {

  private static final String SYMBOL_FILENAME = "symbols.txt";

  @Autowired
  private SymbolSaver symbolSaver;

  @Test
  @SneakyThrows
  public void saveAllDailyCandlesFromFile() {
    symbolSaver.saveAllDailyCandlesFromFile(
        ResourceUtils.getFile("classpath:" + SYMBOL_FILENAME)
    );
  }

  @Test
  @SneakyThrows
  public void saveAllLatestCandles() {
    symbolSaver.saveAllLatestCandles();
  }

  @Test
  public void saveAllIntradayCandlesFromStoredDailyCandles() {
    symbolSaver.saveAllIntradayCandlesFromStoredDailyCandles();
  }

  @Test
  @SneakyThrows
  public void saveAllIntradayCandlesFromFile() {
    symbolSaver.saveAllIntradayCandlesFromFile(
        ResourceUtils.getFile("classpath:" + SYMBOL_FILENAME)
    );
  }

}
