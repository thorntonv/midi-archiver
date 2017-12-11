package org.midiarchiver.core.test.tools;

import javax.sound.midi.*;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * A utility to record a sample of midi data that can be used for testing.
 */
public class TestSampleRecorder {

  public static void main(String[] args) throws MidiUnavailableException, FileNotFoundException, InterruptedException {
    if (args.length < 3) {
      System.out.println("Usage: " + TestSampleRecorder.class.getName() +
          " [deviceVendor] [deviceName] [outputFilename]");
      return;
    }
    String deviceVendor = args[0];
    String deviceName = args[1];
    String outputFilename = args[2];
    for (MidiDevice.Info deviceInfo : MidiSystem.getMidiDeviceInfo()) {
      MidiDevice inputDevice = MidiSystem.getMidiDevice(deviceInfo);
      if (inputDevice.getMaxTransmitters() == 0) {
        continue;
      }
      System.out.println(deviceInfo.getVendor() + " " + deviceInfo.getName());
      if (deviceInfo.getName().equalsIgnoreCase(deviceName)
          && deviceInfo.getVendor().equalsIgnoreCase(deviceVendor)) {
        inputDevice.open();
        Transmitter transmitter = inputDevice.getTransmitter();
        DataOutputReceiver receiver =
            new DataOutputReceiver(new DataOutputStream(new FileOutputStream(outputFilename)));
        transmitter.setReceiver(receiver);
        System.out.println("Recording ... ");
        Thread.sleep(10 * 1000);
        inputDevice.close();
        receiver.close();
        System.out.println("Done recording");
      }
    }
  }
}
