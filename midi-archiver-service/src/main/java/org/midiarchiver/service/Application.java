package org.midiarchiver.service;

import org.midiarchiver.core.MidiArchiverService;
import org.midiarchiver.service.spring.MidiArchiverServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@Import(MidiArchiverServiceConfig.class)
@EnableAutoConfiguration
@EnableAsync
@EnableScheduling
public class Application {

  @Autowired
  private MidiArchiverService midiArchiverService;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Scheduled(fixedDelay = 20 * 1000)
  public void checkForNewDevices() {
    midiArchiverService.checkForNewDevices();
  }
}