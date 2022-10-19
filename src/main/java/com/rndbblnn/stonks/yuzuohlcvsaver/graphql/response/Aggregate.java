
package com.rndbblnn.stonks.yuzuohlcvsaver.graphql.response;

import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class Aggregate {

    private ZonedDateTime time;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double volume;

}
