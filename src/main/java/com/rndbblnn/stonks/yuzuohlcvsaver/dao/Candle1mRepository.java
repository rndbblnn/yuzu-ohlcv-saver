package com.rndbblnn.stonks.yuzuohlcvsaver.dao;

import com.rndbblnn.stonks.commons.entity.Candle1mEntity;
import java.time.LocalDateTime;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface Candle1mRepository extends PagingAndSortingRepository<Candle1mEntity, Long> {

  boolean existsTickEntityByTickTimeAndSymbol(LocalDateTime tickTime, String symbol);

}
