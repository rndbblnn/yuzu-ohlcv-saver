package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.rndbblnn.stonks.commons.dto.TimeframeEnum;
import com.rndbblnn.stonks.commons.entity.Candle1mEntity;
import com.rndbblnn.stonks.commons.entity.CandleDailyEntity;
import com.rndbblnn.stonks.commons.utils.CandleUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CandleResampler {

  public void resampleFromDaily(List<CandleDailyEntity> entityList) {

    if (entityList.stream().collect(Collectors.groupingBy(CandleDailyEntity::getSymbol)).size() > 1) {
      throw new RuntimeException("Cannot resample for multiple symbols");
    }

    CandleUtils.resample(entityList, TimeframeEnum.tf_w);

  }

  public void resampleFrom1Minute(List<Candle1mEntity> entityList) {
    if (entityList.stream().collect(Collectors.groupingBy(Candle1mEntity::getSymbol)).size() > 1) {
      throw new RuntimeException("Cannot resample for multiple symbols");
    }
  }


}
