package com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.util.CollectionUtils;

@Data
public class OhlcvRequest implements GraphQLRequest {

  private static final DateTimeFormatter YUZU_UTC_DATETIMEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'.000Z'");

  private int limit = 500;
  private Period period = Period.DAY;
  private ZonedDateTime after;
  private ZonedDateTime before;
  private List<String> symbols;

  public enum Period {
    MINUTE,
    HOUR,
    DAY
  }

  private static final String QUERY_TEMPLATE =
      "query {\n"
          + "  securities(input: { ${symbol} }) {\n"
          + "    symbol\n"
          + "    aggregates(input: { limit: ${limit},  period: ${period} ${after} ${before}}) {\n"
          + "      time\n"
          + "      open\n"
          + "      high\n"
          + "      low\n"
          + "      close\n"
          + "      volume\n"
          + "    }\n"
          + "  }\n"
          + "}";

  public String toQueryStr() {
    return
        new StringSubstitutor(
            new HashMap<>() {{
              put("symbol", (!CollectionUtils.isEmpty(symbols))
                  ? "symbols: [" +
                    symbols.stream().map(s -> "\"" + s + "\"")
                        .collect(Collectors.joining(","))
                  + "]"
                  : "");
              put("limit", String.valueOf(limit));
              put("period", period.name());
              put("before", (before != null) ? ", before: \"" + toDateStr(before) + "\"" : "");
              put("after", (after != null) ? ", after: \"" + toDateStr(after) + "\"" : "");
            }}
        )
            .replace(QUERY_TEMPLATE);
  }

  public static void main(String... strings) {
    System.out.println(
        new OhlcvRequest()
            .setPeriod(Period.DAY)
            .setAfter(null)
            .toQueryStr()
    );
  }

  private static final String toDateStr(ZonedDateTime localDateTime) {

    String s = localDateTime.withSecond(0)
        .withNano(0)
        .format(YUZU_UTC_DATETIMEFORMATTER);

//    System.out.println(localDateTime + " -> " + s);

    return s;
  }
}
