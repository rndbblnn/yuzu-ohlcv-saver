package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest.Period;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Data;
import java.time.ZonedDateTime;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class YuzuClientTest extends BaseIntegrationTest {

  @Autowired
  private YuzuClient yuzuClient;

  @Test
  public void test() {

    Data d = yuzuClient.query(
        new OhlcvRequest()
            .setSymbols(Lists.newArrayList("AEHR"))
            .setPeriod(Period.MINUTE)
//            .setAfter(ZonedDateTime.of(2022,11,16, ))
            .setAfter(null),
        Data.class
    );

    System.out.println(d.getSecurities().size());
    System.out.println(d.getSecurities().get(0).getSymbol());
    System.out.println(d.getSecurities().get(0).getAggregates().size());
        System.out.println(d.getSecurities().get(0).getAggregates().get(0).getTime() + " " +
        d.getSecurities().get(0).getAggregates().get(d.getSecurities().get(0).getAggregates().size()-1).getTime());

  }

}
