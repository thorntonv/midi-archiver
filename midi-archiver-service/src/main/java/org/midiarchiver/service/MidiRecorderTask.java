package org.midiarchiver.service;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import org.midiarchiver.core.MidiRecorder;

public class MidiRecorderTask implements Runnable {

  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final Info midiDeviceInfo;

  public MidiRecorderTask(final Info midiDeviceInfo) {
    this.midiDeviceInfo = midiDeviceInfo;
  }

  @Override
  public void run() {
    while (!shutdown.get()) {
      try (MidiDevice midiDevice = MidiSystem.getMidiDevice(midiDeviceInfo)) {
        MidiRecorder midiRecorder = new MidiRecorder(midiDevice);
        midiRecorder.startRecording();
        System.out.println("Recording on device " + midiDeviceInfo.getName());
        Thread.sleep(60 * 1000);
        midiRecorder.stopRecording();
      } catch (Exception ex) {
        try {
          Thread.sleep(5 * 100);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  public void shutdown() {
    shutdown.set(true);
  }
}
