package com.rndbblnn.stonks.yuzuohlcvsaver;

import com.rndbblnn.stonks.yuzuohlcvsaver.graphql.request.GraphQLRequest;
import io.aexp.nodes.graphql.GraphQLRequestEntity;
import io.aexp.nodes.graphql.GraphQLResponseEntity;
import io.aexp.nodes.graphql.GraphQLTemplate;
import io.aexp.nodes.graphql.Variable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class YuzuClient {

  @Value("${api.yuzu.api-key}")
  private String yuzuApiKey;

  @SneakyThrows
  @Async
  public <T> CompletableFuture<? extends T> query(GraphQLRequest request, Class<T> responseClass) {

//    System.out.println("after: " + ((OhlcvRequest) request).getAfter() + ", before: " +
//        ((OhlcvRequest) request).getBefore());

    return
        CompletableFuture.completedFuture(
            this.doQuery(request, responseClass).getResponse()
        );

  }

  private <T> GraphQLResponseEntity<T> doQuery(GraphQLRequest request, Class<T> responseClass) {
    GraphQLTemplate graphQLTemplate = new GraphQLTemplate();
    boolean nullResponse = false;

//    String  q= request.toQueryStr().replaceAll("\\r|\\n", "");
    String q = request.toQueryStr();
//    System.err.println(q);

    for (int i = 0; i < 3; i++) {
      GraphQLResponseEntity<T> response = null;
      try {
        response =
            graphQLTemplate.query(
                GraphQLRequestEntity.Builder()
                    .url("https://graph.yuzu.dev/graphql")
                    .variables(new Variable<>("timeFormat", "MM/dd/yyyy"))
                    .scalars(BigDecimal.class)
                    .headers(new HashMap<>() {{
                      put("Authorization", "Bearer " + yuzuApiKey);
                      put("Content-Type", "application/json");
                    }})
                    .request(q)
                    .build(),
                responseClass
            );
      } catch (Exception e) {
        System.err.println("Try #" + i);
        e.printStackTrace();
      }
      if (response != null && response.getResponse() != null) {
        return response;
      } else {
        System.err.println("null response #" + i);
        nullResponse = true;
      }
    }

    if (nullResponse) {
      return new GraphQLResponseEntity();
    }
    throw new RuntimeException("Unable to fetch from yuzu");

  }


}
