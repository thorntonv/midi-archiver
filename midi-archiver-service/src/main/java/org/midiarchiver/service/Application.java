package org.midiarchiver.service;

import org.midiarchiver.core.MidiArchiverService;
import org.midiarchiver.service.spring.MidiArchiverServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;

@SpringBootApplication
@Import(MidiArchiverServiceConfig.class)
public class Application implements ApplicationListener<ContextClosedEvent> {

  @Autowired
  private MidiArchiverService midiArchiverService;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    midiArchiverService.shutdown();
  }
}
