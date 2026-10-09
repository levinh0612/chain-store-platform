package com.chain.platform.core;

import org.springframework.boot.SpringApplication;

public class TestCoreAppApplication {

  public static void main(String[] args) {
    SpringApplication.from(CoreAppApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
