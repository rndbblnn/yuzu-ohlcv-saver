package com.rndbblnn.stonks.yuzuohlcvsaver.dao;

import com.rndbblnn.stonks.commons.entity.CandleDailyEntity;
import java.time.LocalDateTime;
import org.springframework.data.repository.CrudRepository;

public interface CandleDailyRepository extends CrudRepository<CandleDailyEntity, Long> {

  boolean existsTickEntityByTickTimeAndSymbol(LocalDateTime tickTime, String symbol);

}
