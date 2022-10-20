package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.rndbblnn.stonks.yuzuohlcvsaver.dao.Candle1mRepository;
import com.rndbblnn.stonks.yuzuohlcvsaver.dao.CandleDailyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class SymbolSaverAsync {

  private final YuzuClient yuzuClient;
  private final CandleDailyRepository candleDailyRepository;
  private final Candle1mRepository candle1mRepository;
//
//  @Async
//  @SneakyThrows
//  public CompletableFuture<Data> saveIntradayCandles(String symbol, ZonedDateTime zonedDateTime) {
//
//    ZonedDateTime from = zonedDateTime
//        .withHour(13)
//        .withMinute(30)
//        .withSecond(0);
//    ZonedDateTime to = zonedDateTime
//        .withHour(20)
//        .withMinute(0)
//        .withSecond(0);
//
//    if (candle1mRepository.existsTickEntityByTickTimeAndSymbol(
//        from.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime(), symbol)
//        &&
//        candle1mRepository.existsTickEntityByTickTimeAndSymbol(
//            to.minusMinutes(1).withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime(), symbol)) {
//      log.warn("Skipping {} @ {}", symbol, from);
//
//      return CompletableFuture.completedFuture(null);
//    }
//
//    log.info("querying... [symbol:{}, from:{}, to:{}]", symbol, zonedDateTime.toLocalDateTime(), zonedDateTime.toLocalDateTime());
//
//    CompletableFuture<Data> completableFuture = (CompletableFuture<Data>) yuzuClient.query(
//        new OhlcvRequest()
//            .setSymbols(Lists.newArrayList(symbol))
//            .setPeriod(Period.MINUTE)
//            .setAfter(from)
//            .setBefore(to),
//        Data.class
//    );
//
//    Data data = completableFuture.get();
//    if (data == null) {
//      log.error("data == null {} @ {}", symbol, from);
//      return CompletableFuture.completedFuture(null);
//    }
//    Security security = data.getSecurities().get(0);
//    if (data.getSecurities().size() > 1) {
//      throw new RuntimeException("size > 1");
//    }
//    if (data.getSecurities().get(0).getAggregates().size() == 0) {
//      log.info("\t{} aggregates is empty", symbol);
//      return completableFuture;
//    }
//
//    security.getAggregates()
//        .stream()
//        .filter(agg -> !candle1mRepository.existsTickEntityByTickTimeAndSymbol(
//            agg.getTime().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime(), symbol))
//        .forEach(agg -> {
//
////          System.err.println("\t" + agg.getTime() + " - >" + agg.getTime().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime());
//
//          candle1mRepository.save(
//              (Candle1mEntity) new Candle1mEntity()
//                  .setSymbol(security.getSymbol())
//                  .setOpen(agg.getOpen())
//                  .setHigh(agg.getHigh())
//                  .setLow(agg.getLow())
//                  .setClose(agg.getClose())
//                  .setVolume(agg.getVolume().longValue())
//                  .setTickTime(agg.getTime().withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime())
//                  .setCreated(LocalDateTime.now())
//          );
//        });
//
//    return completableFuture;
//  }

}
