package org.midiarchiver.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import org.midiarchiver.core.MidiRecorder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);

    ExecutorService executorService = Executors.newCachedThreadPool();
    for (Info midiDeviceInfo : MidiSystem.getMidiDeviceInfo()) {
      executorService.submit(new MidiRecorderTask(midiDeviceInfo));
    }
  }
}
