package org.midiarchiver.core;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

/**
 * A service that wraps static {@link MidiSystem} utility methods.
 */
public class MidiSystemService {

  public MidiDevice.Info[] getMidiDeviceInfo() {
    return MidiSystem.getMidiDeviceInfo();
  }

  public MidiDevice getMidiDevice(MidiDevice.Info deviceInfo) throws MidiUnavailableException {
    return MidiSystem.getMidiDevice(deviceInfo);
  }
}
