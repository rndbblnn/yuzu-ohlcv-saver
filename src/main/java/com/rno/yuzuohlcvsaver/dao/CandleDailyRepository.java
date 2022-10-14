package com.rno.yuzuohlcvsaver.dao;

import com.rno.yuzuohlcvsaver.dao.entity.CandleDailyEntity;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;

public interface CandleDailyRepository extends CrudRepository<CandleDailyEntity, Long> {

  boolean existsTickEntityByTickTimeAndSymbol(LocalDateTime tickTime, String symbol);

}
