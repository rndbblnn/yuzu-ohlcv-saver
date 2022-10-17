package com.rndbblnn.stonks.yuzuohlcvsaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.rndbblnn")
@EntityScan("com.rndbblnn.*")
public class YuzoOhlcvSaverApplication {

  public static void main(String... strings) {
    System.setProperty("spring.profiles.default", "local");

    SpringApplication.run(YuzoOhlcvSaverApplication.class, strings);
  }

}
