package org.midiarchiver.core;

import java.util.UUID;
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

  public String getDeviceId(final MidiDevice.Info deviceInfo) {
    String deviceIdString = deviceInfo.getVendor() + deviceInfo.getName() + deviceInfo.getVersion();
    return UUID.nameUUIDFromBytes(deviceIdString.getBytes()).toString();
  }
}
