package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.google.common.collect.Lists;
import com.rndbblnn.stonks.commons.dto.CandleDto;
import com.rndbblnn.stonks.commons.utils.DateUtils;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest.Period;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Aggregate;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class SymbolService {

  private final YuzuClient yuzuClient;

  public List<CandleDto> getIntradayCandles(final String symbol, LocalDate from, LocalDate to) {

    List<Aggregate> aggregates = new ArrayList<>();

    LocalDate current = from;
    while (current.isBefore(to)) {

      while (!DateUtils.isMarketDay(current)) {
        current = current.plusDays(1);
      }

      aggregates.addAll(
          this.getIntradayCandlesForDay(symbol,
              current.atStartOfDay(ZoneId.of("UTC")),
              current.atStartOfDay(ZoneId.of("UTC"))
                  .withHour(16)
                  .withMinute(0)
          ));

      aggregates.addAll(
          this.getIntradayCandlesForDay(symbol,
              current.atStartOfDay(ZoneId.of("UTC"))
                  .withHour(16)
                  .withMinute(0),
              current.atStartOfDay(ZoneId.of("UTC"))
                  .plusDays(1)
          ));

      current = current.plusDays(1);
    }

    return aggregates.stream()
        .filter(agg -> agg.getTime().isAfter(agg.getTime().withHour(14).withMinute(29))
            && agg.getTime().isBefore(agg.getTime().withHour(21).withMinute(01)))
        .map(agg -> new CandleDto()
            .setSymbol(symbol)
            .setTickTime(agg.getTime().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
            .setOpen(agg.getOpen())
            .setHigh(agg.getHigh())
            .setLow(agg.getLow())
            .setClose(agg.getClose())
            .setVolume(agg.getVolume().longValue()))
        .collect(Collectors.toList());
  }


  @SneakyThrows
  @Transactional
  private List<Aggregate> getIntradayCandlesForDay(final String symbol, ZonedDateTime from, ZonedDateTime to) {
    log.info("querying... [symbol:{}, period: {}, from:{}, to:{}]", symbol, Period.MINUTE, from, to);

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
      return Collections.emptyList();
    }
    if (data.getSecurities().size() > 1) {
      throw new RuntimeException("size > 1");
    }
    if (data.getSecurities().get(0).getAggregates().size() == 0) {
      log.info("\t{} aggregates is empty", symbol);
      return Collections.emptyList();
    }

    return data.getSecurities().iterator().next().getAggregates();
  }

  public List<CandleDto> getDailyCandles(String symbol, LocalDate from, LocalDate to) {
    log.info("querying... [symbol:{}, period: {}, from:{}, to:{}]", symbol, Period.DAY, from, to);

    Data data = yuzuClient.query(
        new OhlcvRequest()
            .setSymbols(Lists.newArrayList(symbol))
            .setPeriod(Period.DAY)
            .setAfter(from.atStartOfDay().atZone(ZoneId.of("UTC")))
            .setBefore(to.atStartOfDay().atZone(ZoneId.of("UTC"))),
        Data.class
    );

    if (data == null) {
      log.error("data == null {} @ {}", symbol, from);
      return Collections.emptyList();
    }
    if (data.getSecurities().size() > 1) {
      throw new RuntimeException("size > 1");
    }
    if (data.getSecurities().get(0).getAggregates().size() == 0) {
      log.info("\t{} aggregates is empty", symbol);
      return Collections.emptyList();
    }
    return data.getSecurities().iterator().next().getAggregates().stream()
        .map(agg -> new CandleDto()
            .setSymbol(symbol)
            .setTickTime(agg.getTime().toLocalDateTime())
            .setOpen(agg.getOpen())
            .setHigh(agg.getHigh())
            .setLow(agg.getLow())
            .setClose(agg.getClose())
            .setVolume(agg.getVolume().longValue()))
        .collect(Collectors.toList());
  }
}
