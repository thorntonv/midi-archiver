package org.midiarchiver.core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;


public class MidiRecorder {

  private final MidiDevice inputDevice;
  private Sequencer sequencer;

  public MidiRecorder(MidiDevice inputDevice) {
    this.inputDevice = inputDevice;
  }

  public void startRecording()
      throws MidiUnavailableException, InvalidMidiDataException, IOException {
    if (sequencer != null && sequencer.isRecording()) {
      stopRecording();
    }

    inputDevice.open();

    sequencer = MidiSystem.getSequencer();
    sequencer.open();

    Transmitter transmitter = inputDevice.getTransmitter();
    Receiver receiver = sequencer.getReceiver();
    transmitter.setReceiver(receiver);

    Sequence seq = new Sequence(Sequence.PPQ, 24);
    Track currentTrack = seq.createTrack();
    sequencer.setSequence(seq);
    sequencer.setTickPosition(0);
    sequencer.recordEnable(currentTrack, -1);
    sequencer.startRecording();
  }

  public void stopRecording() throws IOException {
    if (sequencer != null && sequencer.isRecording()) {
      sequencer.stopRecording();
      Sequence tmp = sequencer.getSequence();
      MidiSystem.write(tmp, 0, new File(getMidiOutputFileName()));
    }
  }

  private static String getMidiOutputFileName() {
    return new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()) + ".mid";
  }
}
