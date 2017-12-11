package org.midiarchiver.core;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A {@link SequenceWriter} that writes sequences as midi files to the file system.
 */
public class FileSequenceWriter implements SequenceWriter {

  private static final Logger logger = LoggerFactory.getLogger(FileSequenceWriter.class);

  private final String outputDirectoryPath;

  public FileSequenceWriter(String outputDirectoryPath) {
    this.outputDirectoryPath = Preconditions.checkNotNull(outputDirectoryPath);
  }

  /**
   * Writes the given sequence to a file. The file path be in the following pattern:
   * yyyy/MM/dd/hh-mm-ss.mid
   */
  @Override
  public void write(Sequence sequence) throws IOException {
    String outputFilename = getMidiOutputFileName();
    MidiSystem.write(sequence, 1, new File(getMidiOutputFileName()));
    logger.info("Wrote " + outputFilename);
  }

  private String getMidiOutputFileName() {
    File targetDirectory = new File(outputDirectoryPath + File.separator +
        new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
    targetDirectory.mkdirs();
    return targetDirectory.getAbsolutePath() + File.separator +
        new SimpleDateFormat("hh-mm-ss").format(new Date()) + ".mid";
  }
}
