package com.rndbblnn.stonks.yuzuohlcvsaver;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
@ComponentScan("com.rndbblnn")
@EntityScan("com.rndbblnn.*")
@Slf4j
public class YuzoOhlcvSaverApplication {

  @Autowired
  private SymbolSaver symbolSaver;

  public static void main(String... strings) {
    System.setProperty("spring.profiles.default", "local");
    SpringApplication.run(YuzoOhlcvSaverApplication.class, strings);
  }

  @Scheduled(cron = "0 45 11,20 * * *")
  @Transactional
  public void fetchLatestCandles() {
    log.info("Fetching latest candles...");
    symbolSaver.saveAllLatestCandles();
  }

}
