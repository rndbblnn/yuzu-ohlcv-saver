package com.rndbblnn.stonks.yuzuohlcvsaver.web;

import com.rndbblnn.stonks.commons.dto.APIResponse;
import com.rndbblnn.stonks.commons.dto.CandleDto;
import com.rndbblnn.stonks.commons.dto.TimeframeEnum;
import com.rndbblnn.stonks.yuzuohlcvsaver.SymbolService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

  private final SymbolService symbolService;

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

    List<CandleDto> candleDtoList = new ArrayList<>();

    switch (timeframeEnum) {
      case tf_1m:
        candleDtoList = symbolService.getIntradayCandles(symbol, from, to);
        break;
      case tf_d:
        candleDtoList = symbolService.getDailyCandles(symbol, from, to);
        break;
      default:
        throw new UnsupportedOperationException("Unsupport timeframe: " + timeframeEnum.name());
    }

    return ResponseEntity.status(!CollectionUtils.isEmpty(candleDtoList) ? HttpStatus.OK : HttpStatus.NOT_FOUND)
        .body(APIResponse.<List<CandleDto>>builder()
            .payload(candleDtoList)
            .build())
        ;

  }
}
