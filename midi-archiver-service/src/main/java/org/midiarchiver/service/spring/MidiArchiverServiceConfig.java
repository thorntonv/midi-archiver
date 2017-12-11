package org.midiarchiver.service.spring;

import org.midiarchiver.core.MidiArchiverService;
import org.springframework.context.annotation.Bean;

public class MidiArchiverServiceConfig {

  private int newDeviceCheckIntervalMillis = 20 * 1000;
  private int stopRecordingDelayMillis = 5 * 1000;

  @Bean
  public MidiArchiverService midiArchiverService() {
    MidiArchiverService midiArchiverService = new MidiArchiverService(
        newDeviceCheckIntervalMillis, stopRecordingDelayMillis);
    midiArchiverService.start();
    return midiArchiverService;
  }
}
