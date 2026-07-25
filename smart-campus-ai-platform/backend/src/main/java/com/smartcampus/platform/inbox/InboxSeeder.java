package com.smartcampus.platform.inbox;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InboxSeeder {

  @Bean
  public CommandLineRunner seedInbox(InboxService inboxService) {
    return args -> inboxService.seedIfEmpty();
  }
}
