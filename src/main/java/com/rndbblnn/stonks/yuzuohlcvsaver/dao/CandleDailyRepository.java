package com.rndbblnn.stonks.yuzuohlcvsaver.dao;

import com.rndbblnn.stonks.commons.entity.CandleDailyEntity;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CandleDailyRepository extends PagingAndSortingRepository<CandleDailyEntity, Long> {

  boolean existsTickEntityByTickTimeAndSymbol(LocalDateTime tickTime, String symbol);

  Page<CandleDailyEntity> findAllByTickTimeAfter(LocalDateTime tickTime, Pageable query);
}
