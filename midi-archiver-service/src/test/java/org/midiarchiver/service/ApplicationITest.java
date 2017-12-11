package org.midiarchiver.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.midiarchiver.core.MidiArchiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationITest {

  @Autowired
  private MidiArchiverService midiArchiverService;

  @Test
  public void contextLoads() {
    assertNotNull(midiArchiverService);
  }
}
