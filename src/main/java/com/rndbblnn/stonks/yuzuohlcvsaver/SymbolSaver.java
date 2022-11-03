package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.rndbblnn.stonks.commons.entity.Candle1mEntity;
import com.rndbblnn.stonks.commons.entity.CandleDailyEntity;
import com.rndbblnn.stonks.commons.utils.DateUtils;
import com.rndbblnn.stonks.yuzuohlcvsaver.dao.Candle1mRepository;
import com.rndbblnn.stonks.yuzuohlcvsaver.dao.CandleDailyRepository;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest.Period;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Data;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Security;
import java.io.File;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@AllArgsConstructor
@Component
@Slf4j
public class SymbolSaver {

  private final SymbolSaverAsync symbolSaverAsync;

  private final CandleDailyRepository candleDailyRepository;
  private final YuzuClient yuzuClient;
  private final Candle1mRepository candle1mRepository;

  private static Executor taskExecutor = Executors.newFixedThreadPool(5);

  public void saveAllLatestCandles() {
    LocalDateTime currentDate = LocalDateTime.now();
    LocalDateTime latestTickTimeFromDb = candleDailyRepository.findLatestTickTime();

    do {
      latestTickTimeFromDb = latestTickTimeFromDb.minusDays(1);
    } while (!DateUtils.isMarketDay(latestTickTimeFromDb.toLocalDate()));

    // daily
    List<String> latestSymbols = candleDailyRepository.findAllSymbolsByTickTime(latestTickTimeFromDb);

    while (currentDate.isAfter(latestTickTimeFromDb)) {
      final LocalDateTime currentDatef = currentDate;
      Lists.partition(latestSymbols, 450)
          .stream()
          .forEach(symbolList -> {
            this.saveDailyCandles(symbolList,
                currentDatef.atZone(ZoneId.of("UTC")),
                currentDatef.plusDays(1).atZone(ZoneId.of("UTC")));
          });
      currentDate = currentDate.minusDays(1);
    }

    // intra
    this.saveAllIntradayCandles(latestSymbols, currentDate.minusDays(1));
  }

  @SneakyThrows
  public void saveAllDailyCandlesFromFile(File file) {

    List<String> allSymbols = Files.readLines(file, Charset.defaultCharset());

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

            CompletableFuture.supplyAsync(() ->
                    this.saveDailyCandles(Lists.newArrayList(symbol), dateFrom, dateTo),
                taskExecutor
            );
          }
        });
  }

  @SneakyThrows
  public Data saveDailyCandles(List<String> symbolList, ZonedDateTime dateFrom, ZonedDateTime dateTo) {

    dateFrom = DateUtils.trimHoursAndMinutes(dateFrom);
    dateTo = DateUtils.trimHoursAndMinutes(dateTo);

    log.info("querying... [count:{}, from:{}, to:{}]", symbolList.size(), dateFrom, dateTo);

    Data d;
    try {
      d = yuzuClient.query(
          new OhlcvRequest()
              .setSymbols(symbolList)
              .setPeriod(Period.DAY)
              .setAfter(dateFrom)
              .setBefore(dateTo),
          Data.class
      );
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    if (d == null) {
      System.err.println("d== null");
      return null;
    }

    if (CollectionUtils.isEmpty(d.getSecurities())) {
      System.err.println("d.securities empty");
      return null;
    }

    d.getSecurities().stream()
        .forEach(security -> {
          security.getAggregates()
              .stream()
              .filter(agg -> !candleDailyRepository.existsTickEntityByTickTimeAndSymbol(agg.getTime().toLocalDateTime(),
                  security.getSymbol()))
              .forEach(agg -> {
                candleDailyRepository.save(
                    (CandleDailyEntity) new CandleDailyEntity()
                        .setSymbol(security.getSymbol())
                        .setOpen(agg.getOpen())
                        .setHigh(agg.getHigh())
                        .setLow(agg.getLow())
                        .setClose(agg.getClose())
                        .setVolume(agg.getVolume().longValue())
                        .setTickTime(agg.getTime().toLocalDateTime())
                        .setCreated(LocalDateTime.now())
                );
              });
        });

    log.info("DONE [count:{}, from:{}, to:{}]", symbolList.size(), dateFrom, dateTo);

    return d;
  }

  @SneakyThrows
  public void saveAllIntradayCandlesFromStoredDailyCandles() {

    LocalDateTime fromDate = LocalDateTime.now()
//        .minusYears(1)
        .withMonth(5)
        .withDayOfMonth(2)
        .withHour(0)
        .withMinute(0)
        .withSecond(0);

    Pageable pageable = PageRequest.of(
        0,
        10,
        Sort.by("symbol").ascending()
            .and(Sort.by("tickTime").ascending())
    );

    Page<CandleDailyEntity> page;
    do {
      page = candleDailyRepository.findAllByTickTimeAfter(fromDate, pageable);
      this.savePage(page);
    } while ((pageable = page.nextPageable()) != Pageable.unpaged());

  }

  @SneakyThrows
  public void saveAllIntradayCandlesFromFile(File file) {

    this.saveAllIntradayCandles(
        Files.readLines(file, Charset.defaultCharset()),
        LocalDateTime.now()
//        .minusYears(1)
            .withMonth(5)
            .withDayOfMonth(2)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
    );

  }


  @SneakyThrows
  public void saveAllIntradayCandles(List<String> symbolList, LocalDateTime fromDate) {

    int i = 0;
    for (String symbol : symbolList) {

      log.info("Processing {} ({})", symbol, i++);

      Pageable pageable = PageRequest.of(
          0,
          10,
          Sort.by("symbol").ascending()
              .and(Sort.by("tickTime").ascending())
      );

      Page<CandleDailyEntity> page;
      do {
        page = candleDailyRepository.findAllBySymbolAndTickTimeAfter(symbol, fromDate, pageable);
        this.savePage(page);
      } while ((pageable = page.nextPageable()) != Pageable.unpaged());
    }

    CompletableFuture.runAsync(() -> {
              try {
                TimeUnit.HOURS.sleep(10);
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            }
        )
        .join();
  }

  @SneakyThrows
  private void savePage(Page<CandleDailyEntity> page) {
    page.stream()
        .forEach(candleDailyEntity -> {
              CompletableFuture.supplyAsync(() ->
                      this.saveIntradayCandles(
                          candleDailyEntity.getSymbol(),
                          candleDailyEntity.getTickTime().atZone(ZoneOffset.UTC)
                      ),
                  taskExecutor
              );
            }
        );
  }

  @SneakyThrows
  private Data saveIntradayCandles(String symbol, ZonedDateTime zonedDateTime) {

    ZonedDateTime from = zonedDateTime
        .withHour(13)
        .withMinute(30)
        .withSecond(0);
    ZonedDateTime to = zonedDateTime
        .withHour(20)
        .withMinute(0)
        .withSecond(0);

    if (candle1mRepository.existsTickEntityByTickTimeAndSymbol(
        from.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime(), symbol)
        &&
        candle1mRepository.existsTickEntityByTickTimeAndSymbol(
            to.minusMinutes(1).withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime(), symbol)) {
      log.warn("Skipping {} @ {}", symbol, from);
      return null;
    }

//    log.info("querying... [symbol:{}, from:{}, to:{}]", symbol, zonedDateTime, zonedDateTime);

    Data data = yuzuClient.query(
        new OhlcvRequest()
            .setSymbols(Lists.newArrayList(symbol))
            .setPeriod(Period.MINUTE)
            .setAfter(from)
            .setBefore(to),
        Data.class
    );

    if (data == null) {
      log.error("data == null {} @ {}", symbol, from);
      return null;
    }
    Security security = data.getSecurities().get(0);
    if (data.getSecurities().size() > 1) {
      throw new RuntimeException("size > 1");
    }
    if (data.getSecurities().get(0).getAggregates().size() == 0) {
      log.info("\t{} aggregates is empty", symbol);
      return null;
    }

    security.getAggregates()
        .stream()
        .filter(agg -> !candle1mRepository.existsTickEntityByTickTimeAndSymbol(
            agg.getTime().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime(), symbol))
        .forEach(agg -> {

          LocalDateTime tickTime = agg.getTime().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
          log.info("saving ... [symbol:{}, tickTime:{}]", symbol, tickTime);

          candle1mRepository.save(
              (Candle1mEntity) new Candle1mEntity()
                  .setSymbol(security.getSymbol())
                  .setOpen(agg.getOpen())
                  .setHigh(agg.getHigh())
                  .setLow(agg.getLow())
                  .setClose(agg.getClose())
                  .setVolume(agg.getVolume().longValue())
                  .setTickTime(tickTime)
                  .setCreated(LocalDateTime.now())
          );
        });

    return data;
  }
}
