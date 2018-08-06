package org.midiarchiver.service.spring;

import java.io.File;
import java.util.function.Function;
import javax.sound.midi.MidiDevice;
import org.midiarchiver.core.ArchivingReceiver;
import org.midiarchiver.core.FileSequenceWriter;
import org.midiarchiver.core.MidiArchiverService;
import org.midiarchiver.core.MidiSystemService;
import org.midiarchiver.core.SequenceWriter;
import org.springframework.context.annotation.Bean;

public class MidiArchiverServiceConfig {

  private String dataDirectoryPath = "data";
  private int stopRecordingDelayMillis = 5 * 1000;

  @Bean
  public MidiArchiverService midiArchiverService(final MidiSystemService midiSystemService) {
    return new MidiArchiverService(midiSystemService, archivingReceiverFactory(midiSystemService));
  }

  @Bean
  public Function<MidiDevice.Info, ArchivingReceiver> archivingReceiverFactory(
      final MidiSystemService midiSystemService) {
    return (MidiDevice.Info deviceInfo) -> {
      SequenceWriter sequenceWriter = new FileSequenceWriter(dataDirectoryPath + File.separator +
              midiSystemService.getDeviceId(deviceInfo));
      return new ArchivingReceiver(deviceInfo, sequenceWriter, stopRecordingDelayMillis);
    };
  }

  @Bean
  public MidiSystemService midiSystemService() {
    return new MidiSystemService();
  }
}
