package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.google.common.collect.Lists;
import com.rndbblnn.stonks.commons.entity.Candle1mEntity;
import com.rndbblnn.stonks.commons.entity.CandleDailyEntity;
import com.rndbblnn.stonks.yuzuohlcvsaver.dao.Candle1mRepository;
import com.rndbblnn.stonks.yuzuohlcvsaver.dao.CandleDailyRepository;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest.Period;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Data;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Security;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@AllArgsConstructor
@Slf4j
public class SymbolSaverAsync {

  private final YuzuClient yuzuClient;

  private final CandleDailyRepository candleDailyRepository;
  private final Candle1mRepository candle1mRepository;

  @Async
  @SneakyThrows
  public CompletableFuture<Data> saveIntradayCandles(String symbol, ZonedDateTime zonedDateTime) {

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

      return CompletableFuture.completedFuture(null);
    }

    log.info("querying... [symbol:{}, from:{}, to:{}]", symbol, zonedDateTime.toLocalDateTime(), zonedDateTime.toLocalDateTime());

    CompletableFuture<Data> completableFuture = (CompletableFuture<Data>) yuzuClient.query(
        new OhlcvRequest()
            .setSymbols(Lists.newArrayList(symbol))
            .setPeriod(Period.MINUTE)
            .setAfter(from)
            .setBefore(to),
        Data.class
    );

    Data data = completableFuture.get();
    if (data == null) {
      log.error("data == null {} @ {}", symbol, from);
      return CompletableFuture.completedFuture(null);
    }
    Security security = data.getSecurities().get(0);
    if (data.getSecurities().size() > 1) {
      throw new RuntimeException("size > 1");
    }
    if (data.getSecurities().get(0).getAggregates().size() == 0) {
      log.info("\t{} aggregates is empty", symbol);
      return completableFuture;
    }

    security.getAggregates()
        .stream()
        .filter(agg -> !candle1mRepository.existsTickEntityByTickTimeAndSymbol(
            agg.getTime().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime(), symbol))
        .forEach(agg -> {

//          System.err.println("\t" + agg.getTime() + " - >" + agg.getTime().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime());

          candle1mRepository.save(
              (Candle1mEntity) new Candle1mEntity()
                  .setSymbol(security.getSymbol())
                  .setOpen(agg.getOpen())
                  .setHigh(agg.getHigh())
                  .setLow(agg.getLow())
                  .setClose(agg.getClose())
                  .setVolume(agg.getVolume().longValue())
                  .setTickTime(agg.getTime().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime())
                  .setCreated(LocalDateTime.now())
          );
        });

    return completableFuture;
  }

  @Async
  @SneakyThrows
  public CompletableFuture<Object> saveDailyCandles(String symbol, ZonedDateTime dateFrom, ZonedDateTime dateTo) {

    log.info("querying... [symbol:{}, from:{}, to:{}]", symbol, dateFrom, dateTo);

    CompletableFuture<? extends Data> completableFuture = null;
    try {
      completableFuture = yuzuClient.query(
          new OhlcvRequest()
              .setSymbols(Lists.newArrayList(symbol))
              .setPeriod(Period.DAY)
              .setAfter(dateFrom)
              .setBefore(dateTo),
          Data.class
      );
    } catch (Exception e) {
      e.printStackTrace();
      return CompletableFuture.completedFuture(null);
    }

    Data d = completableFuture.get();

    if (d == null) {
      System.err.println("d== null");
      return CompletableFuture.completedFuture(null);
    }

    if (CollectionUtils.isEmpty(d.getSecurities())) {
      System.err.println("d.securities empty");
      return CompletableFuture.completedFuture(null);
    }

    Security security = d.getSecurities().get(0);
    if (d.getSecurities().size() > 1) {
      throw new RuntimeException("size > 1");
    }

    security.getAggregates()
        .stream()
        .filter(agg -> !candleDailyRepository.existsTickEntityByTickTimeAndSymbol(agg.getTime().toLocalDateTime(), symbol))
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

    log.info("\tDONE [symbol:{}, from:{}, to:{}]", symbol, dateFrom, dateTo);

    return CompletableFuture.completedFuture(d);
  }
}
