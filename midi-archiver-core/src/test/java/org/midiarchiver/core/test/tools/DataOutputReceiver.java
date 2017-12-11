package org.midiarchiver.core.test.tools;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Receiver} implementation that writes data to a data output stream.
 */
public class DataOutputReceiver implements Receiver {

  private final DataOutputStream out;
  private List<MidiMessage> messages = new ArrayList<>();
  private List<Long> timeStamps = new ArrayList<>();

  public DataOutputReceiver(final DataOutputStream out) {
    this.out = out;
  }

  @Override
  public synchronized void send(MidiMessage message, long timeStamp) {
    messages.add(message);
    timeStamps.add(timeStamp);
  }

  @Override
  public void close() {
    try {
      out.writeInt(messages.size());
      for (int i = 0; i < messages.size(); i++) {
        MidiMessage message = messages.get(i);
        Long timeStamp = timeStamps.get(i);
        out.writeInt(message.getStatus());
        out.writeInt(message.getLength());
        out.write(message.getMessage());
        out.writeLong(timeStamp);
      }
      out.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
