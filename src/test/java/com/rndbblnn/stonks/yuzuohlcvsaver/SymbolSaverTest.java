package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.google.common.collect.Sets;
import com.rndbblnn.stonks.commons.dao.CandleUtilsRepository;
import com.rndbblnn.stonks.yuzuohlcvsaver.todelete.SecurityTypeEnum;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

public class SymbolSaverTest extends BaseIntegrationTest {

  private static final String SYMBOL_FILENAME = "symbols.txt";
  private static final String BINANCE_USDT_PAIRS_FILENAME = "binance-usdt-pairs.txt";

  @Autowired
  private SymbolSaver symbolSaver;

  @Test
  @SneakyThrows
  public void saveAllDailyCandlesFromFile() {
    symbolSaver.saveAllDailyCandlesFromFile(
        ResourceUtils.getFile("classpath:" + SYMBOL_FILENAME),
        SecurityTypeEnum.US_STOCK
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

  @Test
  public void testsaveIntradayCandles() {
    symbolSaver.saveIntradayCandles("AMPX", ZonedDateTime.of(2022,11,11,0,0,0,0, ZoneId.systemDefault()));
  }

}
