
package com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Aggregate {

    private LocalDateTime time;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double volume;

}
