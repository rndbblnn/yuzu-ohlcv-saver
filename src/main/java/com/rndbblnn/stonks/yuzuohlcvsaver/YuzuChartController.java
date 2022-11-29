package com.rndbblnn.stonks.yuzuohlcvsaver;

import static com.rndbblnn.stonks.commons.utils.CandleUtils.resample;

import com.google.common.collect.Lists;
import com.rndbblnn.stonks.commons.dto.APIResponse;
import com.rndbblnn.stonks.commons.dto.CandleDto;
import com.rndbblnn.stonks.commons.dto.TimeframeEnum;
import com.rndbblnn.stonks.commons.utils.CandleUtils;
import com.rndbblnn.stonks.commons.utils.DateUtils;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest.Period;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Data;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Security;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class YuzuChartController {

  private final YuzuClient yuzuClient;

  @GetMapping("/chart/{symbol}/{timeframe}")
  @SneakyThrows
  public ResponseEntity<APIResponse<List<CandleDto>>> getChart(
      @PathVariable String symbol,
      @PathVariable String timeframe,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate to) {

    TimeframeEnum timeframeEnum = TimeframeEnum.fromTimeframeStr(timeframe);
    if (timeframeEnum == null) {
      log.error("Invalid timeframe: {}", timeframe);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    List<CandleDto> candleList = new ArrayList<>();

    ZonedDateTime zonedDateTimeFrom = ZonedDateTime.of(
        from.atTime(0, 0),
        ZoneId.of("UTC")
    );
    ZonedDateTime zonedDateTimeTo = ZonedDateTime.of(
        to.atTime(0, 0),
        ZoneId.of("UTC")
    );

    int aggSize = 0;
    int offset = 0;

    do {
      Data data = yuzuClient.query(
          new OhlcvRequest()
              .setSymbols(Lists.newArrayList(symbol))
              .setPeriod(Period.MINUTE)
              .setAfter(zonedDateTimeFrom)
              .setBefore(zonedDateTimeTo)
              .setOffset(offset),
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

      aggSize = security.getAggregates().size();
      offset += aggSize;

      candleList.addAll(
          security.getAggregates()
              .stream()
              .filter(agg -> agg.getTime().isAfter(zonedDateTimeFrom.withHour(14).withMinute(29))
                  && agg.getTime().isBefore(zonedDateTimeFrom.withHour(21).withMinute(01)))
              .map(agg -> new CandleDto()
                  .setOpen(agg.getOpen())
                  .setHigh(agg.getHigh())
                  .setLow(agg.getLow())
                  .setClose(agg.getClose())
                  .setVolume(agg.getVolume().longValue()))
              .collect(Collectors.toList())
      );

    } while (aggSize >= 500);





    switch (timeframeEnum) {
      case tf_d:
      case tf_1m:
      case tf_1h:
        break;
      default:
        log.error("Unsupported timeframe: {}", timeframeEnum);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    return ResponseEntity.status(!CollectionUtils.isEmpty(candleList) ? HttpStatus.OK : HttpStatus.NOT_FOUND)
        .body(APIResponse.<List<CandleDto>>builder()
            .payload(candleList)
            .build())
        ;

  }
}
