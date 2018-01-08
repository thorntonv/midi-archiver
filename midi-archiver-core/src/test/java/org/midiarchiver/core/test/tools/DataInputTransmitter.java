package org.midiarchiver.core.test.tools;

import java.io.DataInputStream;
import java.io.IOException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

/**
 * A {@link Transmitter} that reads data from a data input stream and sends the messages from the
 * stream to the {@link Receiver}.
 */
public class DataInputTransmitter implements Transmitter {

  private static class DataOutputMidiMessage extends ShortMessage {

    protected DataOutputMidiMessage(byte[] data) {
      super(data);
    }

    @Override
    public Object clone() {
      return new DataOutputMidiMessage(getMessage());
    }
  }

  private final DataInputStream in;
  private Receiver receiver;

  public DataInputTransmitter(final DataInputStream in) {
    this.in = in;
  }

  @Override
  public void setReceiver(Receiver receiver) {
    this.receiver = receiver;

    Long currentTimeStamp = null;
    try {
      int messageCount = in.readInt();
      for (int cnt = 1; cnt <= messageCount; cnt++) {
        final long messageStatus = in.readInt();
        final int messageDataLen = in.readInt();
        final byte[] messageData = new byte[messageDataLen];
        in.read(messageData);
        final long timeStamp = in.readLong();
        if(currentTimeStamp != null) {
          long delta = timeStamp - currentTimeStamp;
          try {
            Thread.sleep(delta / 1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        currentTimeStamp = timeStamp;
        receiver.send(new DataOutputMidiMessage(messageData), timeStamp);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public Receiver getReceiver() {
    return receiver;
  }

  @Override
  public void close() {
    try {
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
