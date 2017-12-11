package org.midiarchiver.core;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link MidiArchiverService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MidiArchiverServiceTest {

  private static final int TEST_NEW_DEVICE_CHECK_INTERVAL_MILLIS = 5 * 1000;
  @Mock
  private MidiDevice mockDevice1;
  private MidiDevice.Info mockDeviceInfo1;
  @Mock
  private MidiDevice mockDevice2;
  private MidiDevice.Info mockDeviceInfo2;
  @Mock
  private MidiSystemService mockMidiSystemService;
  @Mock
  private Function<Info, ArchivingReceiver> mockArchivingReceiverFactory;
  @Mock
  private ArchivingReceiver mockArchivingReceiver1;
  @Mock
  private ArchivingReceiver mockArchivingReceiver2;
  @Mock
  private Transmitter mockTransmitter1;
  @Mock
  private Transmitter mockTransmitter2;

  private MidiArchiverService midiArchiverService;

  @Before
  public void setUp() throws MidiUnavailableException {
    mockDeviceInfo1 = new TestMidiDeviceInfo("name1", "vendor1", "", "1");
    mockDeviceInfo2 = new TestMidiDeviceInfo("name2", "vendor2", "", "2");

    when(mockMidiSystemService.getMidiDeviceInfo()).thenReturn(
        new MidiDevice.Info[]{mockDeviceInfo1});
    when(mockMidiSystemService.getMidiDevice(mockDeviceInfo1)).thenReturn(mockDevice1);
    when(mockMidiSystemService.getMidiDevice(mockDeviceInfo2)).thenReturn(mockDevice2);
    when(mockDevice1.getDeviceInfo()).thenReturn(mockDeviceInfo1);
    when(mockDevice2.getDeviceInfo()).thenReturn(mockDeviceInfo2);
    when(mockDevice1.getMaxTransmitters()).thenReturn(-1);
    when(mockDevice2.getMaxTransmitters()).thenReturn(10);
    when(mockDevice1.getTransmitter()).thenReturn(mockTransmitter1);
    when(mockDevice2.getTransmitter()).thenReturn(mockTransmitter2);

    when(mockArchivingReceiverFactory.apply(mockDeviceInfo1)).thenReturn(mockArchivingReceiver1);
    when(mockArchivingReceiverFactory.apply(mockDeviceInfo2)).thenReturn(mockArchivingReceiver2);

    this.midiArchiverService = new MidiArchiverService(
        TEST_NEW_DEVICE_CHECK_INTERVAL_MILLIS, mockMidiSystemService, mockArchivingReceiverFactory);
  }

  @Test
  public void testCheckForNewDevices_newDeviceOnInitialCheck() throws Exception {
    midiArchiverService.checkForNewDevices();
    verifyRecordingStartedOnDevice(
        mockDevice1, mockDeviceInfo1, mockArchivingReceiver1, mockTransmitter1);
  }

  @Test
  public void testCheckForNewDevices_newDeviceAfterInitialCheck() throws Exception {
    midiArchiverService.checkForNewDevices();

    verifyRecordingStartedOnDevice(
        mockDevice1, mockDeviceInfo1, mockArchivingReceiver1, mockTransmitter1);

    when(mockMidiSystemService.getMidiDeviceInfo()).thenReturn(
        new MidiDevice.Info[]{mockDeviceInfo1, mockDeviceInfo2});

    midiArchiverService.checkForNewDevices();

    verifyRecordingStartedOnDevice(
        mockDevice1, mockDeviceInfo1, mockArchivingReceiver1, mockTransmitter1);

    verifyRecordingStartedOnDevice(
        mockDevice2, mockDeviceInfo2, mockArchivingReceiver2, mockTransmitter2);
  }

  @Test
  public void testCheckForNewDevices_deviceRemovedAfterInitialCheck() throws Exception {
    midiArchiverService.checkForNewDevices();

    verifyRecordingStartedOnDevice(
        mockDevice1, mockDeviceInfo1, mockArchivingReceiver1, mockTransmitter1);

    when(mockMidiSystemService.getMidiDeviceInfo()).thenReturn(new MidiDevice.Info[]{});

    midiArchiverService.checkForNewDevices();

    verifyDeviceClosed(mockDevice1, mockArchivingReceiver1);
  }


  @Test
  public void testCheckForNewDevices_deviceUnavailable() throws Exception {
    doThrow(new MidiUnavailableException()).when(mockDevice1).open();
    midiArchiverService.checkForNewDevices();

    verify(mockArchivingReceiverFactory, never()).apply(mockDeviceInfo1);
    verify(mockArchivingReceiverFactory, never()).apply(mockDeviceInfo2);
  }

  @Test
  public void testCheckForNewDevices_noDevices() throws Exception {
    when(mockMidiSystemService.getMidiDeviceInfo()).thenReturn(new MidiDevice.Info[]{});
    midiArchiverService.checkForNewDevices();

    verify(mockArchivingReceiverFactory, never()).apply(mockDeviceInfo1);
    verify(mockArchivingReceiverFactory, never()).apply(mockDeviceInfo2);
  }

  @Test
  public void testShutdown() throws MidiUnavailableException {
    midiArchiverService.checkForNewDevices();

    verifyRecordingStartedOnDevice(
        mockDevice1, mockDeviceInfo1, mockArchivingReceiver1, mockTransmitter1);

    midiArchiverService.shutdown();
    midiArchiverService.run();

    verifyDeviceClosed(mockDevice1, mockArchivingReceiver1);
  }

  private void verifyRecordingStartedOnDevice(MidiDevice mockDevice, MidiDevice.Info mockDeviceInfo,
      ArchivingReceiver mockArchivingReceiver, Transmitter mockTransmitter)
      throws MidiUnavailableException {
    verify(mockArchivingReceiverFactory).apply(mockDeviceInfo);
    verify(mockTransmitter).setReceiver(mockArchivingReceiver);
    verify(mockDevice, atLeastOnce()).open();
    verify(mockDevice, never()).close();
  }

  private void verifyDeviceClosed(MidiDevice mockDevice, ArchivingReceiver mockArchivingReceiver) {
    verify(mockDevice, atLeastOnce()).close();
    verify(mockArchivingReceiver, atLeastOnce()).close();
  }
}