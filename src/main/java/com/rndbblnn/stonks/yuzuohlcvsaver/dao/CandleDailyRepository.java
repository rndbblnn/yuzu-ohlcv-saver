package com.rndbblnn.stonks.yuzuohlcvsaver.dao;

import com.rndbblnn.stonks.commons.entity.CandleDailyEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface CandleDailyRepository extends PagingAndSortingRepository<CandleDailyEntity, Long> {

  boolean existsTickEntityByTickTimeAndSymbol(LocalDateTime tickTime, String symbol);

  Page<CandleDailyEntity> findAllByTickTimeAfter(LocalDateTime tickTime, Pageable query);

  Page<CandleDailyEntity> findAllBySymbolAndTickTimeAfter(String symbol, LocalDateTime tickTime, Pageable query);

  Page<CandleDailyEntity> findAllBySymbol(String symbol, Pageable pageable);

  @Query(nativeQuery = true, value = "SELECT MAX(tick_time) FROM candle_d")
  LocalDateTime findLatestTickTime();

  @Query(nativeQuery = true, value = "SELECT symbol FROM candle_d WHERE tick_time = :tickTime")
  List<String> findAllSymbolsByTickTime(@Param("tickTime") LocalDateTime tickTime);

  @Query(nativeQuery = true, value = "SELECT DISTINCT symbol FROM candle_d ORDER by 1 ASC")
  List<String> findAllSymbols();

}
