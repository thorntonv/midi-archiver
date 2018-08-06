package org.midiarchiver.core;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service that records and archives recorded data on all input midi devices. New devices are
 * detected using a periodic check.
 */
public class MidiArchiverService implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(MidiArchiverService.class);

  private final MidiSystemService midiSystemService;
  private final Function<MidiDevice.Info, ArchivingReceiver> archivingReceiverFactory;
  private final Map<String, Pair<MidiDevice, ArchivingReceiver>> activeDevices = new HashMap<>();

  public MidiArchiverService(
      final MidiSystemService midiSystemService,
      final Function<MidiDevice.Info, ArchivingReceiver> archivingReceiverFactory) {
    this.midiSystemService = Preconditions.checkNotNull(midiSystemService);
    this.archivingReceiverFactory = Preconditions.checkNotNull(archivingReceiverFactory);
  }

  public void checkForNewDevices() {
    logger.info("Checking for new devices");
    Map<String, Pair<MidiDevice, ArchivingReceiver>> newRecordableDevices = new HashMap<>();
    for(MidiDevice midiDevice : getRecordableDevices(midiSystemService)) {
      String deviceId = midiSystemService.getDeviceId(midiDevice.getDeviceInfo());
      Pair<MidiDevice, ArchivingReceiver> recordableDevice = activeDevices.get(deviceId);

      if (recordableDevice == null) {
        try {
          ArchivingReceiver archivingReceiver = startArchiverOnDevice(midiDevice);
          recordableDevice = Pair.of(midiDevice, archivingReceiver);
        } catch (MidiUnavailableException e) {
          logger.debug("Midi device is unavailable", e);
        }
      }
      if(recordableDevice != null) {
        newRecordableDevices.put(deviceId, recordableDevice);
        activeDevices.remove(deviceId);
      }
    }

    // Close devices that are no longer available.
    closeAll(activeDevices.values());

    activeDevices.clear();
    activeDevices.putAll(newRecordableDevices);
  }

  @Override
  public void close() throws Exception {
    logger.info("Closing " + getClass().getSimpleName());
    closeAll(activeDevices.values());
  }

  private static List<MidiDevice> getRecordableDevices(final MidiSystemService midiSystemService) {
    List<MidiDevice> recordableDevices = new ArrayList<>();
    for (Info midiDeviceInfo : midiSystemService.getMidiDeviceInfo()) {
      MidiDevice midiDevice = null;
      try {
        midiDevice = midiSystemService.getMidiDevice(midiDeviceInfo);
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

  private ArchivingReceiver startArchiverOnDevice(final MidiDevice midiDevice)
      throws MidiUnavailableException {
    MidiDevice.Info midiDeviceInfo = midiDevice.getDeviceInfo();
    Transmitter transmitter = midiDevice.getTransmitter();
    String deviceName = midiDeviceInfo.getVendor() + " " + midiDeviceInfo.getName();
    logger.info("Starting archiver on device: " + deviceName);

    ArchivingReceiver archivingReceiver = archivingReceiverFactory.apply(midiDeviceInfo);
    transmitter.setReceiver(archivingReceiver);
    if(!midiDevice.isOpen()) {
      midiDevice.open();
    }
    return archivingReceiver;
  }

  private void closeAll(Collection<Pair<MidiDevice, ArchivingReceiver>> deviceWithReceiverList) {
    for (Pair<MidiDevice, ArchivingReceiver> deviceWithReceiver : deviceWithReceiverList) {
      deviceWithReceiver.getRight().close();
      deviceWithReceiver.getLeft().close();
    }
  }
}
