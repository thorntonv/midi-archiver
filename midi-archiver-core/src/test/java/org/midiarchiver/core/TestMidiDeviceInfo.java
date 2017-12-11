package org.midiarchiver.core;

import javax.sound.midi.MidiDevice;

/**
 * A test {@link MidiDevice.Info} class that exposes a public constructor.
 */
public class TestMidiDeviceInfo extends MidiDevice.Info {

  public TestMidiDeviceInfo(String name, String vendor, String description, String version) {
    super(name, vendor, description, version);
  }
}
