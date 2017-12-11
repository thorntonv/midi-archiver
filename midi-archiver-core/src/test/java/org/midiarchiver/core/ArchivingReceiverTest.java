package org.midiarchiver.core;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.midiarchiver.core.test.tools.DataInputTransmitter;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ArchivingReceiver}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArchivingReceiverTest {

  private static final long TEST_STOP_RECORDING_DELAY_MILLIS = 5 * 1000;
  @Mock
  private SequenceWriter mockSequenceWriter;
  @Mock
  private Timer mockTimer;

  private ArchivingReceiver archivingReceiver;

  @Before
  public void setUp() throws Exception {
    archivingReceiver =
        new ArchivingReceiver("_testDevice", mockSequenceWriter, TEST_STOP_RECORDING_DELAY_MILLIS, mockTimer);
  }

  @Test
  public void testSend_singleNoteRecording() throws InvalidMidiDataException, IOException {
    reset(mockTimer, mockSequenceWriter);
    ArgumentCaptor<TimerTask> timerTaskCaptor = ArgumentCaptor.forClass(TimerTask.class);
    ArgumentCaptor<Sequence> sequenceCaptor = ArgumentCaptor.forClass(Sequence.class);
    long timeStamp = 1000;

    archivingReceiver.send(createNoteOnMessage(), timeStamp);
    archivingReceiver.send(createNoteOffMessage(), timeStamp);
    verify(mockTimer, times(2)).schedule(timerTaskCaptor.capture(), anyLong());
    verify(mockTimer).schedule(timerTaskCaptor.capture(), eq(TEST_STOP_RECORDING_DELAY_MILLIS));
    verify(mockSequenceWriter, never()).write(sequenceCaptor.capture());
    timerTaskCaptor.getValue().run();
    verify(mockSequenceWriter).write(sequenceCaptor.capture());
    Sequence sequence = sequenceCaptor.getValue();
    assertTrue(sequence.getTracks().length > 0);
  }

  @Test
  public void testSend_multipleRecordings() throws InvalidMidiDataException, IOException {
    for(int cnt = 1; cnt <= 5; cnt++) {
      testSend_singleNoteRecording();
    }
  }

  @Test
  public void testSend_recordedMessages() throws IOException {
    ArgumentCaptor<TimerTask> timerTaskCaptor = ArgumentCaptor.forClass(TimerTask.class);
    ArgumentCaptor<Sequence> sequenceCaptor = ArgumentCaptor.forClass(Sequence.class);

    try (DataInputTransmitter dataInputTransmitter = new DataInputTransmitter(
        new DataInputStream(Resources.getResource("recordedMessages.dat").openStream()))) {
      dataInputTransmitter.setReceiver(archivingReceiver);
      verify(mockTimer, atLeastOnce()).schedule(timerTaskCaptor.capture(), anyLong());
      verify(mockSequenceWriter, never()).write(sequenceCaptor.capture());
      timerTaskCaptor.getValue().run();
      verify(mockSequenceWriter).write(sequenceCaptor.capture());
      Sequence sequence = sequenceCaptor.getValue();
      assertTrue(sequence.getTracks().length > 0);
      assertTrue(sequence.getTickLength() > 0);
    }
  }

  @Test
  public void testClose_notRecording() throws IOException {
    archivingReceiver.close();
    verify(mockTimer).cancel();
    verify(mockSequenceWriter, never()).write(any(Sequence.class));
  }

  @Test
  public void testClose_duringRecording() throws InvalidMidiDataException, IOException {
    ArgumentCaptor<Sequence> sequenceCaptor = ArgumentCaptor.forClass(Sequence.class);
    long timeStamp = 1000;

    archivingReceiver.send(createNoteOnMessage(), timeStamp);
    archivingReceiver.send(createNoteOffMessage(), timeStamp);
    verify(mockTimer, times(2)).schedule(any(TimerTask.class), anyLong());
    verify(mockTimer).schedule(any(TimerTask.class), eq(TEST_STOP_RECORDING_DELAY_MILLIS));
    verify(mockSequenceWriter, never()).write(sequenceCaptor.capture());

    archivingReceiver.close();
    verify(mockTimer).cancel();

    // Close should cause the sequence to be written.
    verify(mockSequenceWriter).write(sequenceCaptor.capture());
    Sequence sequence = sequenceCaptor.getValue();
    assertTrue(sequence.getTracks().length > 0);
  }

  private ShortMessage createNoteOnMessage() throws InvalidMidiDataException {
    return new ShortMessage(ShortMessage.NOTE_ON, 1, 0, 0);
  }

  private ShortMessage createNoteOffMessage() throws InvalidMidiDataException {
    return new ShortMessage(ShortMessage.NOTE_OFF, 1, 0, 0);
  }
}
