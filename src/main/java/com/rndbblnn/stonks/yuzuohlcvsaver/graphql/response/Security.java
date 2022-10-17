
package com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response;

import java.util.List;
import lombok.Data;

@Data
public class Security {

    private String symbol;
    private List<Aggregate> aggregates = null;

}
