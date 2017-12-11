package org.midiarchiver.core;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A {@link Receiver} that archives midi data. When a note is played on the device this receiver
 * starts recording to a sequencer. If no notes are played for a set amount of time the recorded
 * sequence will be written to the provided {@link SequenceWriter}. A new recording will start when
 * the next note is played.
 */
public class ArchivingReceiver implements Receiver {

  private static final Logger logger = LoggerFactory.getLogger(ArchivingReceiver.class);

  private static final int NOTE_ON_STOP_RECORDING_DELAY_MILLIS = 20 * 1000;

  private final class StopRecordingTimerTask extends TimerTask {
    @Override
    public void run() {
      stopRecording();
    }
  }

  private final MidiDevice.Info deviceInfo;
  private final long stopRecordingDelayMillis;
  private final SequenceWriter sequenceWriter;
  private final Timer stopRecordingTimer;

  private Sequencer sequencer;
  private Receiver receiver;
  private TimerTask stopRecordingTimerTask;

  public ArchivingReceiver(final MidiDevice.Info deviceInfo, final SequenceWriter sequenceWriter,
                           final long stopRecordingDelayMillis) {
    this(deviceInfo, sequenceWriter, stopRecordingDelayMillis, new Timer());
  }

  @VisibleForTesting
  ArchivingReceiver(final MidiDevice.Info deviceInfo, final SequenceWriter sequenceWriter,
                    final long stopRecordingDelayMillis, final Timer stopRecordingTimer) {
    this.deviceInfo = Preconditions.checkNotNull(deviceInfo);
    this.sequenceWriter = Preconditions.checkNotNull(sequenceWriter);
    this.stopRecordingDelayMillis = stopRecordingDelayMillis;
    this.stopRecordingTimer = Preconditions.checkNotNull(stopRecordingTimer);
  }

  /**
   * Handles a message sent from the {@link Transmitter} midi device.
   */
  @Override
  public synchronized void send(MidiMessage message, long timeStamp) {
    if (message instanceof ShortMessage) {
      ShortMessage shortMessage = (ShortMessage) message;
      switch (shortMessage.getCommand()) {
        case ShortMessage.NOTE_ON:
          if (sequencer == null || receiver == null || !sequencer.isRecording()) {
            startRecording();
          }
          extendStopRecordingTimer(NOTE_ON_STOP_RECORDING_DELAY_MILLIS);
          break;
        case ShortMessage.NOTE_OFF:
          extendStopRecordingTimer(stopRecordingDelayMillis);
          break;
      }
    }

    if (sequencer != null && sequencer.isRecording()) {
      receiver.send(message, timeStamp);
    }
  }

  /**
   * Closes and released devices. If a recording is in progress it will be stoped and the recorded
   * data will be archived.
   */
  @Override
  public synchronized void close() {
    stopRecording();
    stopRecordingTimer.cancel();
  }

  private void extendStopRecordingTimer(long newStopRecordingDelayMillis) {
    if (stopRecordingTimerTask != null) {
      stopRecordingTimerTask.cancel();
    }
    stopRecordingTimerTask = new StopRecordingTimerTask();
    stopRecordingTimer.schedule(stopRecordingTimerTask, newStopRecordingDelayMillis);
  }

  private static Sequencer prepareAndOpenNewSequencer() throws MidiUnavailableException, InvalidMidiDataException {
    Sequencer sequencer = MidiSystem.getSequencer();
    sequencer.open();

    Sequence seq = new Sequence(Sequence.SMPTE_24, 24);
    Track currentTrack = seq.createTrack();
    sequencer.setSequence(seq);
    sequencer.setTickPosition(0);
    sequencer.recordEnable(currentTrack, -1);
    return sequencer;
  }

  private synchronized void startRecording() {
    try {
      sequencer = prepareAndOpenNewSequencer();
      receiver = sequencer.getReceiver();
      sequencer.startRecording();
      logger.info(getDeviceName() + " - Recording started");
    } catch (MidiUnavailableException | InvalidMidiDataException e) {
      logger.warn("An error occurred while starting recording on device " + getDeviceName(), e);
    }
  }

  private synchronized void stopRecording() {
    try {
      logger.info(getDeviceName() + " - Recording stopped");
      if (receiver != null) {
        receiver.close();
        receiver = null;
      }
      if (sequencer != null) {
        if (sequencer.isRecording()) {
          sequencer.stopRecording();
          sequenceWriter.write(sequencer.getSequence());
        }
        sequencer.close();
        sequencer = null;
      }
    } catch (IOException e) {
      logger.warn("An error occurred while stopping recording on device " + getDeviceName(), e);
    }
  }

  private String getDeviceName() {
    return deviceInfo.getVendor() + " " + deviceInfo.getName();
  }
}
