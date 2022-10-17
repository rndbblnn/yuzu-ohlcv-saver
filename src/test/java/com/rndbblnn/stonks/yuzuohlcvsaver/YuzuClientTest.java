package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.OhlcvRequest.Period;
import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response.Data;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class YuzuClientTest extends BaseIntegrationTest {

  @Autowired
  private YuzuClient yuzuClient;

  @Test
  public void test() {

    CompletableFuture<? extends Data> completableFuture =
        yuzuClient.query(
        new OhlcvRequest()
            .setPeriod(Period.DAY)
            .setAfter(null),
        Data.class
    );

    Data d = completableFuture.join();

    System.out.println(d.getSecurities().size());
    System.out.println(d.getSecurities().get(0).getSymbol());
    System.out.println(d.getSecurities().get(0).getAggregates().size());
        System.out.println(d.getSecurities().get(0).getAggregates().get(0).getTime() + " " +
        d.getSecurities().get(0).getAggregates().get(d.getSecurities().get(0).getAggregates().size()-1).getTime());

  }

}
