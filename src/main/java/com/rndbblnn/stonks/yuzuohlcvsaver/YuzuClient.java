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
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class YuzuClient {

  @Value("${api.yuzu.api-key}")
  private String yuzuApiKey;

  @SneakyThrows
  public <T> CompletableFuture<? extends T> query(GraphQLRequest request, Class<T> responseClass) {
    GraphQLTemplate graphQLTemplate = new GraphQLTemplate();

//    String  q= request.toQueryStr().replaceAll("\\r|\\n", "");
    String q = request.toQueryStr();
//    System.err.println(q);

    GraphQLResponseEntity<T> responseEntity =
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

    return CompletableFuture.completedFuture(responseEntity.getResponse());

  }


}
