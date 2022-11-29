package com.rndbblnn.stonks.yuzuohlcvsaver.dao;

import com.google.common.collect.Sets;
import com.rndbblnn.stonks.commons.utils.DateUtils;
import com.rndbblnn.stonks.yuzuohlcvsaver.todelete.SecurityTypeEnum;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CandleUtilsRepository {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public List<Pair> findMissingDailyCandles(Set<String> symbolSet) {

    List<Pair> missingSymbolList = new ArrayList<>();

    LocalDate currentDate = LocalDate.of(2018, 1, 1);
    while (currentDate.isBefore(LocalDate.now())) {
      currentDate = currentDate.plusDays(1);

      if (!DateUtils.isMarketDay(currentDate)) {
        continue;
      }

      final LocalDate currentDatef = currentDate;
      Set<String> existingSymbolsForDate =
          namedParameterJdbcTemplate.query(
                  "SELECT symbol "
                      + "FROM candle_d "
                      + "WHERE symbol IN (:symbolList) AND tick_time = :currentDate",
                  new MapSqlParameterSource()
                      .addValue("symbolList", symbolSet)
                      .addValue("currentDate", currentDate),
                  (rs, rownum) -> rs.getString(1))
              .stream().collect(Collectors.toSet());

      missingSymbolList.addAll(
          Sets.difference(symbolSet, existingSymbolsForDate).stream()
              .map(symbol -> ImmutablePair.of(symbol, currentDatef))
              .collect(Collectors.toList())
      );
    }

    return missingSymbolList;
  }


}
