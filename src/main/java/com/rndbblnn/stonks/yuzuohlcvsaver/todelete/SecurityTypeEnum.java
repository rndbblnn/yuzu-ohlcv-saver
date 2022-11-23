package com.rndbblnn.stonks.yuzuohlcvsaver.todelete;

public enum SecurityTypeEnum {
  US_STOCK(1),
  CRYPTO_BINANCE(2);

  private final int value;

  SecurityTypeEnum(int value) {
    this.value = value;
  }
}
