package org.midiarchiver.core;

import javax.sound.midi.Sequence;
import java.io.IOException;

/**
 * An interface for writing a {@link Sequence}.
 */
public interface SequenceWriter {

  void write(Sequence sequence) throws IOException;
}
