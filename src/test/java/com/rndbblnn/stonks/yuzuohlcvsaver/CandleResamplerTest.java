package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.rndbblnn.stonks.commons.entity.CandleDailyEntity;
import com.rndbblnn.stonks.yuzuohlcvsaver.dao.CandleDailyRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
public class CandleResamplerTest extends BaseIntegrationTest {

  @Autowired
  private CandleResampler candleResampler;

  @Autowired
  private CandleDailyRepository candleDailyRepository;

  @Test
  @SneakyThrows
  public void testResampleFromDaily() {
    candleDailyRepository.findAllSymbols().stream()
        .forEach(symbol -> {
          log.info("resammpling {}", symbol);
          Page<CandleDailyEntity> page = candleDailyRepository.findAllBySymbol(symbol, Pageable.unpaged());
          candleResampler.resampleFromDaily(page.toList());
        });
  }

}
