package com.rndbblnn.stonks.yuzuohlcvsaver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SymbolSaverTest extends BaseIntegrationTest{

  @Autowired
  private SymbolSaver symbolSaver;

  @Test
  public void saveAllFromFile() {
    symbolSaver.saveAllFromFile();
  }

}
