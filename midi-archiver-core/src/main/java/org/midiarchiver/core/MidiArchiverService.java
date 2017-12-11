package org.midiarchiver.core;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MidiArchiverService extends Thread {

  private static final Logger logger = LoggerFactory.getLogger(MidiArchiverService.class);

  private final long stopRecordingDelayMillis;
  private final long newDeviceCheckIntervalMillis;
  private final AtomicBoolean shutdown = new AtomicBoolean(false);

  public MidiArchiverService(final long newDeviceCheckIntervalMillis,
                             final long stopRecordingDelayMillis) {
    this.newDeviceCheckIntervalMillis = newDeviceCheckIntervalMillis;
    this.stopRecordingDelayMillis = stopRecordingDelayMillis;
  }

  @Override
  public void run() {
    logger.info(MidiArchiverService.class.getSimpleName() + " started");
    Map<String, Pair<MidiDevice, ArchivingReceiver>> recordableDevices = new HashMap<>();

    try {
      while (!shutdown.get()) {
        logger.info("Checking for new devices");
        List<MidiDevice> newRecordableDevices = getRecordableDevices();

        for (MidiDevice midiDevice : newRecordableDevices) {
          String deviceId = getDeviceId(midiDevice.getDeviceInfo());
          if (!recordableDevices.containsKey(deviceId)) {
            try {
              ArchivingReceiver archivingReceiver = startArchiverOnDevice(midiDevice);
              recordableDevices.put(deviceId, Pair.of(midiDevice, archivingReceiver));
            } catch (MidiUnavailableException e) {
              logger.debug("Midi device is unavailable", e);
            }
          }
        }
        Thread.sleep(newDeviceCheckIntervalMillis);
      }
    } catch (InterruptedException e) {
      logger.warn(MidiArchiverService.class.getSimpleName() + " was interrupted", e);
    }

    closeAll(recordableDevices.values());

    logger.info(MidiArchiverService.class.getSimpleName() + " finished");
  }

  public void shutdown() {
    shutdown.set(true);
  }

  private String getDeviceId(Info info) {
    return info.getVendor() + "_" + info.getName() + "_" + info.getVersion();
  }

  private static List<MidiDevice> getRecordableDevices() {
    List<MidiDevice> recordableDevices = new ArrayList<>();
    for (Info midiDeviceInfo : MidiSystem.getMidiDeviceInfo()) {

      MidiDevice midiDevice = null;
      try {
        midiDevice = MidiSystem.getMidiDevice(midiDeviceInfo);
        if (!midiDevice.isOpen()) {
          midiDevice.open();
        }
        if (midiDevice.getMaxTransmitters() != 0) {
          recordableDevices.add(midiDevice);
        }
      } catch (MidiUnavailableException e) {
        if (midiDevice != null && midiDevice.isOpen()) {
          midiDevice.close();
        }
      }
    }
    return recordableDevices;
  }

  private ArchivingReceiver startArchiverOnDevice(MidiDevice midiDevice) throws MidiUnavailableException {
    MidiDevice.Info midiDeviceInfo = midiDevice.getDeviceInfo();
    Transmitter transmitter = midiDevice.getTransmitter();
    String deviceName = midiDeviceInfo.getVendor() + " " + midiDeviceInfo.getName();
    logger.info("Starting archiver on device: " + deviceName);

    ArchivingReceiver archivingReceiver = new ArchivingReceiver(deviceName, new FileSequenceWriter(
        midiDeviceInfo.getVendor() + File.separator
            + midiDeviceInfo.getName()), stopRecordingDelayMillis);
    transmitter.setReceiver(archivingReceiver);
    midiDevice.open();
    return archivingReceiver;
  }

  private void closeAll(Collection<Pair<MidiDevice, ArchivingReceiver>> deviceWithReceiverList) {
    for (Pair<MidiDevice, ArchivingReceiver> deviceWithReceiver : deviceWithReceiverList) {
      deviceWithReceiver.getRight().close();
      deviceWithReceiver.getLeft().close();
    }
  }
}
