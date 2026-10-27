package freerails.util.ui;

import junit.framework.TestCase;

import java.io.IOException;

public class SoundManagerTest extends TestCase {

    public void testMissingSoundResourceIsReported() {
        try {
            SoundManager.getInstance().addClip("/missing-test-sound.wav");
            fail("Expected IOException");
        } catch (IOException expected) {
        } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
            fail("Unexpected audio format exception");
        }
    }
}
