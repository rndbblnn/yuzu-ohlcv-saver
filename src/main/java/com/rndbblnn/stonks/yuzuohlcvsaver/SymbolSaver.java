package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.google.common.io.Files;
import com.rndbblnn.stonks.commons.entity.CandleDailyEntity;
import com.rndbblnn.stonks.yuzuohlcvsaver.dao.CandleDailyRepository;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@AllArgsConstructor
@Component
@Slf4j
public class SymbolSaver {

  private static final String SYMBOL_FILENAME = "symbols.txt";

  private final SymbolSaverAsync symbolSaverAsync;

  private final YuzuClient yuzuClient;

  private final CandleDailyRepository candleDailyRepository;


  @SneakyThrows
  public void saveAllFromFile() {

    List<String> allSymbols =
        Files.readLines(
            ResourceUtils.getFile("classpath:" + SYMBOL_FILENAME),
            Charset.defaultCharset());

    allSymbols.stream()
        .forEach(symbol -> {
          for (int i = 3; i >= 0; i--) {
            ZonedDateTime dateFrom = ZonedDateTime.now(ZoneOffset.UTC).minus(i, ChronoUnit.YEARS)
                .withDayOfYear(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

            ZonedDateTime dateTo = dateFrom.plus(1, ChronoUnit.YEARS)
                .minusDays(1)
                .withHour(23)
                .withMinute(23)
                .withSecond(59)
                .withNano(999);

            symbolSaverAsync.saveDailyCandles(symbol, dateFrom, dateTo);
          }
        });
  }

  @SneakyThrows
  public void saveCandles1m() {

    LocalDateTime fromDate = LocalDateTime.now()
//        .minusYears(1)
        .withMonth(5)
        .withDayOfMonth(2)
        .withHour(0)
        .withMinute(0)
        .withSecond(0);

    Pageable query = PageRequest.of(
        0,
        10,
        Sort.by("symbol").ascending()
            .and(Sort.by("tickTime").ascending())
    );

    Page<CandleDailyEntity> page;
    do {
      page = candleDailyRepository.findAllByTickTimeAfter(fromDate, query);

      final int pageSize = page.getSize();

      CompletableFuture.allOf(
          page.stream()
              .map(candleDailyEntity ->
                  symbolSaverAsync.saveIntradayCandles(candleDailyEntity.getSymbol(),
                      candleDailyEntity.getTickTime().atZone(ZoneOffset.UTC))
              )
              .toArray(arr -> new CompletableFuture[pageSize])
      ).join();

    } while ((query = page.nextPageable()) != Pageable.unpaged());

  }

//
//  public void saveAll1m() {
////    after:"2022-10-14T13:30:00.000Z", before:"2022-10-14T20:00:00.000Z"
//
//    LocalDateTime dateCur = LocalDateTime.now();
//    LocalDateTime dateFrom = LocalDateTime.now().minus(3, ChronoUnit.YEARS)
//        .withDayOfYear(1)
//        .withHour(0)
//        .withMinute(0)
//        .withSecond(0)
//        .withNano(0);
//
//    while (dateCur.isAfter(dateFrom)) {
//
//    }
//
//    LocalDateTime dateFrom = LocalDateTime.now().minus(i, ChronoUnit.YEARS)
//        .withDayOfYear(1)
//        .withHour(0)
//        .withMinute(0)
//        .withSecond(0)
//        .withNano(0);
//
//    LocalDateTime dateTo = dateFrom.plus(1, ChronoUnit.YEARS)
//        .minusDays(1)
//        .withHour(23)
//        .withMinute(23)
//        .withSecond(59)
//        .withNano(999);
//
//    log.info("querying... [symbol:{}, from:{}, to:{}]", symbol, dateFrom, dateTo);
//
//    CompletableFuture<? extends Data> completableFuture = null;
//    try {
//      completableFuture = yuzuClient.query(
//          new OhlcvRequest()
//              .setSymbols(Lists.newArrayList(symbol))
//              .setPeriod(Period.DAY)
//              .setAfter(dateFrom)
//              .setBefore(dateTo),
//          Data.class
//      );
//    } catch (Exception e) {
//      e.printStackTrace();
//      continue;
//    }
//
//    Data d = completableFuture.join();
//
//    if (d == null) {
//      System.err.println("d== null");
//      continue;
//
//    }
//
//    Security security = d.getSecurities().get(0);
//    if (d.getSecurities().size() > 1) {
//      throw new RuntimeException("size > 1");
//    }
//
//    security.getAggregates()
//        .stream()
//        .filter(agg -> !candleDailyRepository.existsTickEntityByTickTimeAndSymbol(agg.getTime(), symbol))
//        .forEach(agg -> {
//          candleDailyRepository.save(
//              (CandleDailyEntity) new CandleDailyEntity()
//                  .setSymbol(security.getSymbol())
//                  .setOpen(agg.getOpen())
//                  .setHigh(agg.getHigh())
//                  .setLow(agg.getLow())
//                  .setClose(agg.getClose())
//                  .setVolume(agg.getVolume().longValue())
//                  .setTickTime(agg.getTime())
//                  .setCreated(LocalDateTime.now())
//          );
//        });
//
//    try {
//      TimeUnit.MILLISECONDS.sleep(3000);
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
//  }

  private class TenantAware {

    public void runAsTenant(CandleDailyEntity tenant, Object o) {
    }
  }

}
